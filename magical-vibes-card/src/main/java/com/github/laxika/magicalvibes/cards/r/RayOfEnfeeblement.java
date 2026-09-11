package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "116")
public class RayOfEnfeeblement extends Card {

    public RayOfEnfeeblement() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new TargetPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                new BoostTargetCreatureEffect(-4, -1),
                new BoostTargetCreatureEffect(-4, -4)
        ));
    }
}
