with src as
    (SELECT  'Product ' || x AS name
     FROM generate_series(1, 10) AS x)
INSERT INTO products(name)
SELECT name FROM src
WHERE not exists (SELECT 1 FROM products as p WHERE p.name = src.name);