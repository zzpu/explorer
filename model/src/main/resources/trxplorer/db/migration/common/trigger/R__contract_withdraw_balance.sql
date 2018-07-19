DELIMITER $$

DROP TRIGGER IF EXISTS contract_withdraw_balance_AFTER_INSERT$$

CREATE DEFINER = CURRENT_USER TRIGGER `contract_withdraw_balance_AFTER_INSERT` AFTER INSERT ON `contract_withdraw_balance` FOR EACH ROW
BEGIN
insert ignore into sync_account(address,date_created,origin,tx_timestamp) values(new.owner_address,now(),'contract_withdraw_balance',(select block.timestamp from transaction,block where block.id=transaction.block_id and transaction.id=new.transaction_id));
update `transaction` SET `from`=new.owner_address, `type`=13 where `id`=new.transaction_id;
END$$

DELIMITER ;
