package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "7")
@CardRegistration(set = "DDF", collectorNumber = "14")
@CardRegistration(set = "TSR", collectorNumber = "13")
@CardRegistration(set = "C14", collectorNumber = "68")
public class CelestialCrusader extends Card {

    public CelestialCrusader() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
