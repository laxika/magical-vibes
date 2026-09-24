package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect;

@CardRegistration(set = "MSC", collectorNumber = "512")
public class LokiGodOfLies extends Card {

    public LokiGodOfLies() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect());
    }
}
