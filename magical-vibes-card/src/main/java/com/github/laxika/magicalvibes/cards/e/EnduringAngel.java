package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnduringAngelLifeTotalReplacementEffect;

@CardRegistration(set = "MID", collectorNumber = "17")
public class EnduringAngel extends Card {

    public EnduringAngel() {
        setBackFaceCard(new AngelicEnforcer());

        addEffect(EffectSlot.STATIC, new EnduringAngelLifeTotalReplacementEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AngelicEnforcer";
    }
}
