package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "THB", collectorNumber = "92")
public class EnemyOfEnlightenment extends Card {

    public EnemyOfEnlightenment() {
        Scaled minusOnePerCard = new Scaled(new CardsInHand(CountScope.OPPONENTS), -1);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(minusOnePerCard, minusOnePerCard));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
