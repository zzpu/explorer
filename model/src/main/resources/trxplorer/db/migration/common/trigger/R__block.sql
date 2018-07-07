DELIMITER $$

DROP TRIGGER IF EXISTS block_AFTER_INSERT$$

CREATE DEFINER = CURRENT_USER TRIGGER `block_AFTER_INSERT` AFTER INSERT ON `block` FOR EACH ROW
BEGIN
insert ignore into sync_account(address,date_created,origin,tx_timestamp) values(new.witness_address,now(),'block',new.timestamp);
END$$

DELIMITER ;


DELIMITER $$

DROP TRIGGER IF EXISTS block_BEFORE_INSERT$$

CREATE DEFINER= CURRENT_USER TRIGGER `block_BEFORE_INSERT` BEFORE INSERT ON `block` FOR EACH ROW
BEGIN
 	IF (new.num>0) THEN
	SET NEW.block_time = (select timestampdiff(SECOND,(select timestamp from block where num = new.num -1),new.timestamp));
	END IF;
END$$


DELIMITER ;