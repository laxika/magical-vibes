package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastForSpectacleCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "RNA", collectorNumber = "109")
public class RixMaadiReveler extends Card {

    public RixMaadiReveler() {
        addCastingOption(AlternateHandCast.spectacle("{2}{B}{R}", new OpponentLostLifeThisTurn(1)));

        // When this creature enters, discard a card, then draw a card. If its spectacle cost was
        // paid, instead discard your hand, then draw three cards.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForSpectacleCost(), new DiscardOwnHandThenDrawEffect(new Fixed(3))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new NotCondition(new CastForSpectacleCost()),
                new DiscardCardThenEffect(null, new DrawCardEffect(1), "a card")));
    }
}
