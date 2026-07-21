package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsPutIntoGraveyardByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "HOU", collectorNumber = "35")
public class FrayingSanity extends Card {

    public FrayingSanity() {
        // At the beginning of each end step, enchanted player mills X cards, where X is the number
        // of cards put into their graveyard from anywhere this turn.
        addEffect(EffectSlot.ENCHANTED_PLAYER_END_STEP_TRIGGERED,
                new MillEffect(new CardsPutIntoGraveyardByTargetPlayerThisTurn(), MillRecipient.TARGET_PLAYER));
    }
}
