package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "67")
public class ConfrontThePast extends Card {

    public ConfrontThePast() {
        var opponentPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentIsPlaneswalkerPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target planeswalker card with mana value X or less from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.PLANESWALKER))
                                .targetGraveyard(true)
                                .requiresManaValueAtMostX(true)
                                .build()),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove twice X loyalty counters from target planeswalker an opponent controls",
                        new RemoveCountersFromTargetPermanentEffect(
                                CounterType.LOYALTY, new Scaled(new XValue(), 2), opponentPlaneswalker),
                        new PermanentPredicateTargetFilter(
                                opponentPlaneswalker,
                                "Target must be a planeswalker an opponent controls."))
        )));
    }
}
