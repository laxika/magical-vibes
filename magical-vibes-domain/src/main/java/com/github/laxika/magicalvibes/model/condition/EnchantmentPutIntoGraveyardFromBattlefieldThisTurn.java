package com.github.laxika.magicalvibes.model.condition;

/** An enchantment was put into a player's graveyard from the battlefield this turn. */
public record EnchantmentPutIntoGraveyardFromBattlefieldThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an enchantment was put into your graveyard from the battlefield this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no enchantment was put into your graveyard from the battlefield this turn";
    }
}
