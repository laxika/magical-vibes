package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfFromGraveyardToHandEffect;

@CardRegistration(set = "10E", collectorNumber = "239")
public class SqueeGoblinNabob extends Card {

    public SqueeGoblinNabob() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new MayEffect(new ReturnSelfFromGraveyardToHandEffect(), "Return Squee, Goblin Nabob from your graveyard to your hand?"));
    }
}
