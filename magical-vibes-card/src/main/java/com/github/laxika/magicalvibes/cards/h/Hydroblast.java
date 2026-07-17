package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "94")
public class Hydroblast extends Card {

    public Hydroblast() {
        // "if it's red" is part of the effect, not a targeting restriction: either mode may target
        // any spell / permanent, and does nothing on resolution unless the target is red.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell if it's red",
                        new ConditionalEffect(
                                new TargetSpellMatches(new StackEntryColorInPredicate(Set.of(CardColor.RED))),
                                new CounterSpellEffect())
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target permanent if it's red",
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentColorInPredicate(Set.of(CardColor.RED))),
                                new DestroyTargetPermanentEffect())
                )
        )));
    }
}
