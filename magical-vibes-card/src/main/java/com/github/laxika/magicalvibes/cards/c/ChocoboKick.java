package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "178")
public class ChocoboKick extends Card {

    public ChocoboKick() {
        addEffect(EffectSlot.STATIC, KickerEffect.returning(new PermanentIsLandPredicate(), "a land"));

        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                        new TargetDealsPowerDamageToTargetEffect(),
                        new TargetDealsPowerDamageToTargetEffect(2)));
    }
}
