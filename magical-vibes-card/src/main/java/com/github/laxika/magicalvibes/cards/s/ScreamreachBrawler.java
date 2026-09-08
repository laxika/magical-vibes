package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "155")
public class ScreamreachBrawler extends Card {

    public ScreamreachBrawler() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
    }
}
