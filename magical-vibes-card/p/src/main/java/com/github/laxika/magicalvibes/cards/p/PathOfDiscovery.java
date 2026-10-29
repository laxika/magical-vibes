package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "RIX", collectorNumber = "142")
@CardRegistration(set = "FIC", collectorNumber = "312")
@CardRegistration(set = "MSC", collectorNumber = "179")
@CardRegistration(set = "MSC", collectorNumber = "386")
@CardRegistration(set = "BLC", collectorNumber = "231")
public class PathOfDiscovery extends Card {

    public PathOfDiscovery() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new ExploreEffect());
        // "Whenever a creature you control enters" includes Path itself when it enters as an animated creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new SourceIsCreature(), new ExploreEffect()));
    }
}
