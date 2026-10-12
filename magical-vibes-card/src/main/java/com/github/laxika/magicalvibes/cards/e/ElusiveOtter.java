package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GrovesBounty;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;

@CardRegistration(set = "WOE", collectorNumber = "225")
@CardRegistration(set = "WOE", collectorNumber = "289")
@CardRegistration(set = "SOC", collectorNumber = "306")
public class ElusiveOtter extends Card {

    public ElusiveOtter() {
        setBackFaceCard(new GrovesBounty());
        addCastingOption(new AdventureCast("{X}{G}"));
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "GrovesBounty";
    }
}
