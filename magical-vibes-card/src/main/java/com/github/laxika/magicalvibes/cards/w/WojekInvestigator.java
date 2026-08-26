package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MKM", collectorNumber = "36")
public class WojekInvestigator extends Card {

    public WojekInvestigator() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CreateTokenEffect.ofClueToken(new OpponentsWithMoreCardsInHandThanController()));
    }
}
