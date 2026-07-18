package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "218")
public class RedElementalBlast extends Card {

    public RedElementalBlast() {
        // "blue" is a targeting restriction here (unlike Pyroblast's resolution "if it's blue"):
        // each mode may only target a blue spell / permanent.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target blue spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryColorInPredicate(Set.of(CardColor.BLUE)),
                                "Target spell must be blue.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target blue permanent",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                                "Target permanent must be blue."))
        )));
    }
}
