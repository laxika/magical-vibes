package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseEachPlayerCastCostPerSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;

@CardRegistration(set = "DOM", collectorNumber = "213")
public class DampingSphere extends Card {

    public DampingSphere() {
        addEffect(EffectSlot.STATIC, new ReplaceLandExcessManaWithColorlessEffect());
        addEffect(EffectSlot.STATIC, new IncreaseEachPlayerCastCostPerSpellThisTurnEffect(1));
    }
}
