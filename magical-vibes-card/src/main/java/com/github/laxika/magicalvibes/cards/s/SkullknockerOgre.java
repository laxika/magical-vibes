package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardThenDrawEffect;

@CardRegistration(set = "ELD", collectorNumber = "142")
public class SkullknockerOgre extends Card {

    public SkullknockerOgre() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new TargetPlayerRandomDiscardThenDrawEffect());
    }
}
