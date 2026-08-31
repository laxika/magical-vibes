package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HareRaising;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "WOE", collectorNumber = "233")
public class PollenShieldHare extends Card {

    public PollenShieldHare() {
        setBackFaceCard(new HareRaising());
        addCastingOption(new AdventureCast("{G}"));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsTokenPredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "HareRaising";
    }
}
