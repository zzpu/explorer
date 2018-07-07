CREATE TABLE `voting_round` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `day` INT UNSIGNED NULL,
  `month` INT UNSIGNED NULL,
  `year` INT UNSIGNED NULL,
  `round` INT UNSIGNED NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  PRIMARY KEY (`id`));
