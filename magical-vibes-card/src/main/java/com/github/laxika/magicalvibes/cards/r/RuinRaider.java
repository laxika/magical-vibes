package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;

@CardRegistration(set = "XLN", collectorNumber = "118")
public class RuinRaider extends Card {

    public RuinRaider() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // reveal the top card of your library and put that card into your hand.
        // You lose life equal to the card's mana value.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new RaidConditionalEffect(
                new RevealTopCardPutIntoHandAndLoseLifeEffect()
        ));
    }
}
