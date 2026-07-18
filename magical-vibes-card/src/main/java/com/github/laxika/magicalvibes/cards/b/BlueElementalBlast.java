package com.github.laxika.magicalvibes.cards.b;

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

@CardRegistration(set = "4ED", collectorNumber = "63")
public class BlueElementalBlast extends Card {

    public BlueElementalBlast() {
        // "red" is a targeting restriction here (unlike Hydroblast's resolution "if it's red"):
        // each mode may only target a red spell / permanent.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target red spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryColorInPredicate(Set.of(CardColor.RED)),
                                "Target spell must be red.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target red permanent",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentColorInPredicate(Set.of(CardColor.RED)),
                                "Target permanent must be red."))
        )));
    }
}
