package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "101")
@CardRegistration(set = "OM1", collectorNumber = "107")
public class GrowExtraArms extends Card {

    public GrowExtraArms() {
        addStaticEffect(new ReduceOwnCastCostIfTargetingPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.SPIDER), 1));
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(4, 4));
    }
}
