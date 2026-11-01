package com.tarea.tareajpa;

import jakarta.persistence.*;

public class TareaJPA {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tareaPU");
        EntityManager em = emf.createEntityManager();

        // 1) NEW (transient): el objeto existe, pero JPA aún no lo conoce
        Categoria cat = new Categoria();
        cat.setNombre("Tecnología");

        Producto p1 = new Producto();
        p1.setNombre("Laptop");
        p1.setPrecio(850.0);
        p1.setStock(5);

        Producto p2 = new Producto();
        p2.setNombre("Mouse");
        p2.setPrecio(15.5);
        p2.setStock(40);

        cat.agregarProducto(p1);
        cat.agregarProducto(p2);
        System.out.println("[NEW] ¿Administrada? " + em.contains(cat)); // false

        // 2) MANAGED: persist dentro de una transacción
        em.getTransaction().begin();
        em.persist(cat); // el cascade guarda también los productos
        System.out.println("[MANAGED] ¿Administrada? " + em.contains(cat)); // true
        em.getTransaction().commit();

        // 3) Consultas JPQL
        em.createQuery("SELECT p FROM Producto p WHERE p.precio > :min ORDER BY p.nombre", Producto.class)
                .setParameter("min", 20.0)
                .getResultList()
                .forEach(p -> System.out.println("[JPQL] Producto con precio > 20: " + p.getNombre()));

        Categoria c = em.createQuery(
                "SELECT DISTINCT c FROM Categoria c JOIN FETCH c.productos WHERE c.nombre = :n",
                Categoria.class)
                .setParameter("n", "Tecnología")
                .getSingleResult();
        System.out.println("[JPQL] Productos en la categoría: " + c.getProductos().size());

        Long total = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();
        System.out.println("[JPQL] Total de productos: " + total);

        // 4) DETACHED: ya no se sincroniza con la base de datos
        em.detach(cat);
        System.out.println("[DETACHED] ¿Administrada? " + em.contains(cat)); // false
        cat.setNombre("Tecnología y accesorios");

        // 5) merge: vuelve a estado MANAGED y genera el UPDATE
        em.getTransaction().begin();
        Categoria gestionada = em.merge(cat);
        em.getTransaction().commit();
        System.out.println("[MERGE] Nombre actualizado: " + gestionada.getNombre());

        // 6) REMOVED (comenta este bloque si quieres ver los datos en MySQL)
        em.getTransaction().begin();
        em.remove(gestionada); // elimina también sus productos por cascade
        em.getTransaction().commit();
        System.out.println("[REMOVED] Categoría eliminada");

        em.close();
        emf.close();
    }
}