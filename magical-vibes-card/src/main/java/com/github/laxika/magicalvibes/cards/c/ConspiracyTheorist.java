package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfDiscardedCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "STX", collectorNumber = "94")
public class ConspiracyTheorist extends Card {

    public ConspiracyTheorist() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{1}", new DiscardAndDrawCardEffect(), "Pay {1} and discard a card to draw a card?"));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new MayEffect(
                new ExileOneOfDiscardedCardsFromGraveyardEffect(),
                "Exile one of the discarded nonland cards?"));
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                false, new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                false, false, 0, null, false, true, false));
    }
}
