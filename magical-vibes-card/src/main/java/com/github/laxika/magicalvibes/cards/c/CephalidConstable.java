package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;

import java.util.List;

public class CephalidConstable extends Card {

    public CephalidConstable() {
        super("Cephalid Constable", CardType.CREATURE, "{1}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.OCTOPUS, CardSubtype.WIZARD));
        setCardText("Whenever Cephalid Constable deals combat damage to a player, return up to that many target permanents that player controls to their owner's hand.");
        setPower(1);
        setToughness(1);
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ReturnPermanentsOnCombatDamageToPlayerEffect());
    }
}
