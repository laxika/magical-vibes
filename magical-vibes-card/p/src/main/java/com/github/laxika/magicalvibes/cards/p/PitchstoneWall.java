package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDiscardedCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "110")
public class PitchstoneWall extends Card {

    public PitchstoneWall() {
        addTrigger(Triggers.youDiscardCard(),
                new MayEffect(
                        new SacrificeSelfThenEffect(new ReturnDiscardedCardFromGraveyardToHandEffect()),
                        "Sacrifice Pitchstone Wall to return the discarded card to your hand?"));
    }
}
