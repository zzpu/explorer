ALTER TABLE `block` 
ADD COLUMN `confirmed` TINYINT(1) NULL DEFAULT NULL AFTER `block_time`;
