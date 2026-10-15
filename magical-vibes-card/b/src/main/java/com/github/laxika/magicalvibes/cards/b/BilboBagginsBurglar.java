package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TakeAGlance;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOB", collectorNumber = "34")
public class BilboBagginsBurglar extends Card {

    public BilboBagginsBurglar() {
        setBackFaceCard(new TakeAGlance());
        addCastingOption(new AdventureCast("{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "TakeAGlance";
    }
}
