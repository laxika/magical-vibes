package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsXEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "4")
public class KozilekTheGreatDistortion extends Card {

    public KozilekTheGreatDistortion() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_SELF_CAST, new ConditionalEffect(
                new CardsInHandAtMost(6),
                new DrawCardEffect(new Max(
                        new Fixed(0),
                        new Sum(new Fixed(7), new Scaled(cardsInHand, -1))))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null, false, 1, false, true),
                        new CounterSpellIfManaValueEqualsXEffect()),
                "Discard a card with mana value X: Counter target spell with mana value X."
        ));
    }
}
