package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "SNC", collectorNumber = "147")
public class FreelanceMuscle extends Card {

    public FreelanceMuscle() {
        // X is the greatest power or toughness among other creatures controlled by this creature's controller.
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
        var greatestPowerOrToughness = new Max(
                new GreatestPowerAmongControlled(otherCreatures),
                new GreatestToughnessAmongControlled(otherCreatures));
        addEffect(EffectSlot.ON_ATTACK,
                new BoostSelfEffect(greatestPowerOrToughness, greatestPowerOrToughness));
        addEffect(EffectSlot.ON_BLOCK,
                new BoostSelfEffect(greatestPowerOrToughness, greatestPowerOrToughness));
    }
}
