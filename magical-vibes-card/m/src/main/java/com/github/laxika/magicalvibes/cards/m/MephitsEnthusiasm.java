package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "60")
public class MephitsEnthusiasm extends Card {

    public MephitsEnthusiasm() {
        var creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate()));
        target(new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker, "Target must be a creature or planeswalker"))
                .addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
        addSpellEffect(new ConditionalEffect(new EventValueAtLeast(1), SequenceEffect.of(
                new NoteAmountEffect(new EventValue()),
                RegisterDelayedControllerSpellCastTriggerEffect.boon(new CardTypePredicate(CardType.CREATURE),
                        List.of(new PerpetuallyBoostTriggeringCardEffect(AmountThisWay.noted(), new Fixed(0)))))));
    }
}
