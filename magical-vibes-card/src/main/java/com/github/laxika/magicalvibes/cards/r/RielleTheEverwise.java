package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "203")
public class RielleTheEverwise extends Card {

    public RielleTheEverwise() {
        CardsInGraveyard instantsAndSorceries = new CardsInGraveyard(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY))), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(instantsAndSorceries, new Fixed(0)));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new OncePerTurnTriggerEffect(new DrawCardEffect(new EventValue())));
    }
}
