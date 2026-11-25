package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "22")
public class MoltenImpact extends Card {

    public MoltenImpact() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(4));
        var opponentCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addSpellEffect(new ConditionalEffect(new EventValueAtLeast(1), SequenceEffect.of(
                new NoteAmountEffect(new EventValue()),
                RegisterDelayedControllerSpellCastTriggerEffect.boon(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))),
                        List.of(DealDamageEffect.toTargetCreatureOrPlaneswalker(AmountThisWay.noted(), opponentCreatureOrPlaneswalker)))
                        .withTargetFilter(new PermanentPredicateTargetFilter(opponentCreatureOrPlaneswalker,
                                "Target must be a creature or planeswalker an opponent controls")))));
    }
}
