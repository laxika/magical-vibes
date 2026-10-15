package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesArtifactAndNonartifactCreatureEffect;

@CardRegistration(set = "AER", collectorNumber = "68")
public class PerilousPredicament extends Card {

    public PerilousPredicament() {
        addEffect(EffectSlot.SPELL, new EachOpponentSacrificesArtifactAndNonartifactCreatureEffect());
    }
}
