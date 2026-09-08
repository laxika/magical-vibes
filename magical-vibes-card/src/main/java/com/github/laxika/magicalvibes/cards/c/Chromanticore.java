package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "144")
public class Chromanticore extends Card {

    public Chromanticore() {
        addCastingOption(new BestowCast("{2}{W}{U}{B}{R}{G}"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        4, 4,
                        Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.VIGILANCE,
                                Keyword.TRAMPLE, Keyword.LIFELINK),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
