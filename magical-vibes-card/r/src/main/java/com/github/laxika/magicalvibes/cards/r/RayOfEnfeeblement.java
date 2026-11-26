package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "116")
public class RayOfEnfeeblement extends Card {

    public RayOfEnfeeblement() {
        target(TargetFilters.creature()).addSpellEffect(new ConditionalReplacementEffect(
                new TargetPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                BoostEffect.target(-4, -1),
                BoostEffect.target(-4, -4)
        ));
    }
}
