package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "175")
public class VivienMonstersAdvocate extends Card {

    public VivienMonstersAdvocate() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(Set.of(CardType.CREATURE)));

        addActivatedAbility(new ActivatedAbility(
                1,
                List.of(new ChooseOneEffect(List.of(
                        counterChoice("Reach", CounterType.REACH),
                        counterChoice("Vigilance", CounterType.VIGILANCE),
                        counterChoice("Trample", CounterType.TRAMPLE)
                ))),
                "+1: Create a 3/3 green Beast creature token. Put your choice of a reach counter, a vigilance counter, or a trample counter on it."));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new RegisterDelayedControllerSpellCastTriggerEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(new TargetSpellManaValue(), false, -1))),
                        true,
                        false)),
                "-2: When you next cast a creature spell this turn, search your library for a creature card with lesser mana value, put it onto the battlefield, then shuffle."));
    }

    private static ChooseOneEffect.ChooseOneOption counterChoice(String label, CounterType counterType) {
        return new ChooseOneEffect.ChooseOneOption(label, List.of(
                new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()),
                new PutCountersOnCreatedPermanentsEffect(counterType, new Fixed(1))));
    }
}
