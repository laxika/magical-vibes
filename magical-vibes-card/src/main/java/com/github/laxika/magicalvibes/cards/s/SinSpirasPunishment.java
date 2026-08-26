package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect;

@CardRegistration(set = "FIN", collectorNumber = "242")
public class SinSpirasPunishment extends Card {

    public SinSpirasPunishment() {
        var effect = new ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        addEffect(EffectSlot.ON_ATTACK, effect);
    }
}
