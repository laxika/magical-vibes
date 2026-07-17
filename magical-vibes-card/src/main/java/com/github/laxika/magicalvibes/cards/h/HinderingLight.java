package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "173")
public class HinderingLight extends Card {

    public HinderingLight() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAnyOfPredicate(List.of(
                        new StackEntryTargetsYouPredicate(),
                        new StackEntryTargetsYourPermanentPredicate()
                )),
                "Target must be a spell that targets you or a permanent you control."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect())
          .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
