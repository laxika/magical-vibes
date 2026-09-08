package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "115")
public class SkewerTheCritics extends Card {

    public SkewerTheCritics() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{R}")),
                new OpponentLostLifeThisTurn(1),
                false));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
