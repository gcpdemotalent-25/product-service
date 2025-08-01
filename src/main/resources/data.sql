-- On supprime les données existantes pour éviter les doublons à chaque redémarrage
-- ATTENTION: Ne faites cela que pour un environnement de développement !
DELETE FROM produit;

-- On insère de nouvelles données
INSERT INTO produit (nom, prix) VALUES
                                    ('Ordinateur Portable Pro 15"', 1499.99),
                                    ('Smartphone X-Series', 799.50),
                                    ('Casque Audio sans fil', 199.00),
                                    ('Clavier Mécanique Gamer', 120.00),
                                    ('Souris Ergonomique', 45.99),
                                    ('Écran 27" 4K', 450.00);