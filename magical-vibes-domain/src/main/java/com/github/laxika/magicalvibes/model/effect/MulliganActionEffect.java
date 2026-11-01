package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;

/**
 * A static ability of a card in its owner's hand that offers an action while that player decides whether
 * to mulligan (Serum Powder, No-Regrets Egret). Its text names the hand, so it functions only there
 * (CR 113.6b). Read by {@code MulliganService}.
 */
public interface MulliganActionEffect extends CardEffect {

    /** The prompt suffix offered with the action. */
    String mulliganActionDescription();

    @Override
    default AbilityZones staticAbilityZones() {
        return AbilityZones.HAND;
    }
}
