package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeekTheHeart;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "HOB", collectorNumber = "170")
public class TheArkenstone extends Card {

    public TheArkenstone() {
        setBackFaceCard(new SeekTheHeart());
        addCastingOption(new AdventureCast("{2}{W}"));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DrawCardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "SeekTheHeart";
    }
}
