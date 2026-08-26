package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "99")
public class FirefrightMage extends Card {

    public FirefrightMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentColorInPredicate(Set.of(CardColor.RED))
                                )),
                                "artifact creatures and/or red creatures")
                ),
                "{1}{R}, {T}, Discard a card: Target creature can't be blocked this turn except by artifact creatures and/or red creatures.",
                TargetFilters.creature()
        ));
    }
}
