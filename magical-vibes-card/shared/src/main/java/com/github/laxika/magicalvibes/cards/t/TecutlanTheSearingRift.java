package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class TecutlanTheSearingRift extends Card {

    public TecutlanTheSearingRift() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED).withProducingSourceForSpellCastTriggers()),
                "{T}: Add {R}."));
        addTrigger(Triggers.youCastSpell(),
                SpellCastTriggerEffect.usingManaProducedBySource(
                        new CardIsPermanentPredicate(),
                        List.of(new DiscoverEffect(new TargetSpellManaValue()))));
    }
}
