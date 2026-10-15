package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "182")
@CardRegistration(set = "KLD", collectorNumber = "156")
public class GhirapurGuide extends Card {

    public GhirapurGuide() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentPowerAtLeastPredicate(3),
                        "creatures with power 3 or greater")),
                "{2}{G}: Target creature you control can't be blocked by creatures with power 2 or less this turn.",
                TargetFilters.creatureYouControl()));
    }
}
