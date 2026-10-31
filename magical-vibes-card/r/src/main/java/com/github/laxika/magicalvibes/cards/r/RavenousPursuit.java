package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YMID", collectorNumber = "54")
public class RavenousPursuit extends Card {

    public RavenousPursuit() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect(
                                new CardTypePredicate(CardType.CREATURE)));
    }
}
