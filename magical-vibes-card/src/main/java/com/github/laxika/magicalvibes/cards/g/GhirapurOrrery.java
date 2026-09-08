package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;

@CardRegistration(set = "KLD", collectorNumber = "216")
public class GhirapurOrrery extends Card {

    public GhirapurOrrery() {
        addEffect(EffectSlot.STATIC, new EachPlayerPlaysAdditionalLandEffect());
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandEmpty(), new DrawCardForTargetPlayerEffect(3)));
    }
}
