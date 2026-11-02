package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "49")
@CardRegistration(set = "LCC", collectorNumber = "81")
public class FranciscoFowlMarauder extends Card {

    public FranciscoFowlMarauder() {
        // This creature can't block.
        addStaticEffect(new CantBlockEffect());

        // Whenever one or more Pirates you control deal damage to a player, Francisco explores.
        addTrigger(Triggers.creaturesYouControlDealDamageToPlayer(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                        new ExploreEffect()));
    }
}
