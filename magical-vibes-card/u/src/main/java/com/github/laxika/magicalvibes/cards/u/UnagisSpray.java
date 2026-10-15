package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "97")
public class UnagisSpray extends Card {

    public UnagisSpray() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, 0))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ControlsPermanent(new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.FISH,
                                CardSubtype.OCTOPUS,
                                CardSubtype.OTTER,
                                CardSubtype.SEAL,
                                CardSubtype.SERPENT,
                                CardSubtype.WHALE))),
                        new DrawCardEffect()));
    }
}
