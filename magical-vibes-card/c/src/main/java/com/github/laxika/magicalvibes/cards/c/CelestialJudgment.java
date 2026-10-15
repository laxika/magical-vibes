package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CelestialJudgmentEffect;

@CardRegistration(set = "MIC", collectorNumber = "5")
@CardRegistration(set = "MIC", collectorNumber = "43")
public class CelestialJudgment extends Card {

    public CelestialJudgment() {
        addEffect(EffectSlot.SPELL, new CelestialJudgmentEffect());
    }
}
