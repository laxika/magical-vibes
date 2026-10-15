package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EasyPickings;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "HOB", collectorNumber = "99")
public class GloinTheMighty extends Card {

    public GloinTheMighty() {
        setBackFaceCard(new EasyPickings());
        addCastingOption(new AdventureCast("{2}{R}"));
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new AwardManaEffect(ManaColor.RED, 2));
    }

    @Override
    public String getBackFaceClassName() {
        return "EasyPickings";
    }
}
