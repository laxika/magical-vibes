package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BanishingCoils;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;

@CardRegistration(set = "HBG", collectorNumber = "90")
public class GuardianNaga extends Card {

    public GuardianNaga() {
        setBackFaceCard(new BanishingCoils());
        addCastingOption(new AdventureCast("{2}{W}"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(), new PreventAllDamageEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "BanishingCoils";
    }
}
