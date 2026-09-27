package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "NCC", collectorNumber = "50")
@CardRegistration(set = "NCC", collectorNumber = "150")
public class RainOfRiches extends Card {

    public RainOfRiches() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofTreasureToken(2));
        addEffect(EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA, new CascadeEffect());
    }
}
