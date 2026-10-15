package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ALL", collectorNumber = "61a")
@CardRegistration(set = "ALL", collectorNumber = "61b")
public class StenchOfDecay extends Card {

    public StenchOfDecay() {
        // Nonartifact creatures get -1/-1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1, new PermanentNotPredicate(new PermanentIsArtifactPredicate())));
    }
}
