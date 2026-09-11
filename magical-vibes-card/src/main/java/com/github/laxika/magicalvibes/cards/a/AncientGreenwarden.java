package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "178")
public class AncientGreenwarden extends Card {

    public AncientGreenwarden() {
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());
        addEffect(EffectSlot.STATIC, new ETBDoubleTriggerEffect(
                new CardTypePredicate(CardType.LAND), false));
    }
}
