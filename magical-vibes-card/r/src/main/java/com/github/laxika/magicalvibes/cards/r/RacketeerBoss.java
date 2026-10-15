package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "25")
public class RacketeerBoss extends Card {

    public RacketeerBoss() {
        CreateTokenEffect createTreasure = CreateTokenEffect.ofTreasureToken(1);
        SequenceEffect treasureAbility = SequenceEffect.of(
                createTreasure,
                new RemovePerpetualTriggeredAbilityEffect(EffectSlot.ON_SELF_CAST, SequenceEffect.class));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect(
                        2,
                        EffectSlot.ON_SELF_CAST,
                        List.of(treasureAbility),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER)))));
    }
}
