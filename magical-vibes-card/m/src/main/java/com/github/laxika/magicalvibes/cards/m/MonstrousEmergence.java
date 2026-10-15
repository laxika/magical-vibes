package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ChosenCreatureOrRevealedCardPower;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOrRevealCreatureCardCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "191")
public class MonstrousEmergence extends Card {

    public MonstrousEmergence() {
        addEffect(EffectSlot.SPELL, new ChooseCreatureOrRevealCreatureCardCost());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(
                        new ChosenCreatureOrRevealedCardPower()));
    }
}
