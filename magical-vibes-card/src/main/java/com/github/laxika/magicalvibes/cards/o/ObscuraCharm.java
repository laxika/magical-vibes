package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "208")
public class ObscuraCharm extends Card {

    public ObscuraCharm() {
        // Choose one —
        // • Return target multicolored permanent card with mana value 3 or less from your graveyard
        //   to the battlefield tapped.
        // • Counter target instant or sorcery spell.
        // • Destroy target creature or planeswalker with mana value 3 or less.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target multicolored permanent card with mana value 3 or less from your graveyard to the battlefield tapped",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardIsMulticoloredPredicate(),
                                        new CardIsPermanentPredicate(),
                                        new CardMaxManaValuePredicate(3))))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target instant or sorcery spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.SORCERY_SPELL)),
                                "Target must be an instant or sorcery spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature or planeswalker with mana value 3 or less",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsPlaneswalkerPredicate())),
                                        new PermanentMaxManaValuePredicate(3))),
                                "Target must be a creature or planeswalker with mana value 3 or less."))
        )));
    }
}
