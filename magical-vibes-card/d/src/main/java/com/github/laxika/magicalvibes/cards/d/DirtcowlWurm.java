package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "TMP", collectorNumber = "221")
@CardRegistration(set = "BRB", collectorNumber = "19")
public class DirtcowlWurm extends Card {

    public DirtcowlWurm() {
        // Whenever an opponent plays a land, put a +1/+1 counter on this creature.
        // "Plays a land" — not a land merely entering, so this uses the land-play slot.
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
