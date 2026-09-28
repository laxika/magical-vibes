package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PileGroupingOrGuessCountThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "SLD", collectorNumber = "1582")
public class TheCelestialToymaker extends Card {

    public TheCelestialToymaker() {
        addEffect(EffectSlot.ON_ATTACK, new RevealTopCardsAndSeparateEffect(
                3, CardPileDisposition.HAND_AND_EXILE_WITH_FACE_DOWN_PILE,
                true, false, true, true));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new LoseLifeEffect(
                new Scaled(new PileGroupingOrGuessCountThisTurn(), 2),
                LoseLifeRecipient.EACH_OPPONENT));
    }
}
