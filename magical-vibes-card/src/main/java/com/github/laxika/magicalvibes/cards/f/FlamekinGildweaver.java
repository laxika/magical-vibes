package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "ECL", collectorNumber = "140")
public class FlamekinGildweaver extends Card {

    public FlamekinGildweaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(1));
    }
}
