package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerCantPlayLandsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "127")
public class AggressiveMining extends Card {

    public AggressiveMining() {
        addEffect(EffectSlot.STATIC, new ControllerCantPlayLandsEffect());
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                        new DrawCardEffect(2)),
                "Sacrifice a land: Draw two cards. Activate only once each turn.", 1));
    }
}
