package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "3")
public class CelestialArchon extends Card {

    public CelestialArchon() {
        addCastingOption(new BestowCast("{5}{W}{W}"));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                4, 4, Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE));
    }
}
