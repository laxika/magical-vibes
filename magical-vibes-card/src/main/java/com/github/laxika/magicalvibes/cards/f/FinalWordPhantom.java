package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MKC", collectorNumber = "22")
@CardRegistration(set = "MKC", collectorNumber = "332")
public class FinalWordPhantom extends Card {

    public FinalWordPhantom() {
        // During each opponent's end step, you may cast spells as though they had flash.
        addEffect(EffectSlot.OPPONENT_END_STEP_TRIGGERED, new MayEffect(
                new GrantFlashToSpellsThisTurnEffect(),
                "Cast spells as though they had flash?"));
    }
}
