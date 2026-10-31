package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardGreatestManaValueCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YOTJ", collectorNumber = "23")
public class IntrudersInquisition extends Card {

    public IntrudersInquisition() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(TargetDealsPowerDamageToTargetEffect.recordingExcessDamage())
                .addSpellEffect(new ConditionalEffect(
                        new EventValueAtLeast(1), DiscardGreatestManaValueCardEffect.forTargetPermanentController()));
    }
}
