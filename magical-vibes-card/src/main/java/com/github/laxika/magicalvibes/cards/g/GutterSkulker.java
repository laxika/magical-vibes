package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfAttackingAloneEffect;

@CardRegistration(set = "VOW", collectorNumber = "62")
public class GutterSkulker extends Card {

    public GutterSkulker() {
        setBackFaceCard(new GutterShortcut());
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfAttackingAloneEffect());
        addCastingOption(new DisturbCast("{3}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GutterShortcut";
    }
}
