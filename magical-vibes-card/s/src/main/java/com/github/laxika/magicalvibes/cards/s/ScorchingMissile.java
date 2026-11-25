package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "ODY", collectorNumber = "219")
public class ScorchingMissile extends Card {

    public ScorchingMissile() {
        addSpellEffect(DealDamageEffect.toTargetPlayerOrPlaneswalker(4));
        addCastingOption(new FlashbackCast("{9}{R}"));
    }
}
