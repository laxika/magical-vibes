package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "ODY", collectorNumber = "251")
@CardRegistration(set = "SLD", collectorNumber = "1466")
public class MomentsPeace extends Card {

    public MomentsPeace() {
        addSpellEffect(PreventDamageEffect.allCombat());
        addCastingOption(new FlashbackCast("{2}{G}"));
    }
}
