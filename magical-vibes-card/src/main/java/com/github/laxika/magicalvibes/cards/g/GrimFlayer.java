package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "EMN", collectorNumber = "184")
public class GrimFlayer extends Card {

    public GrimFlayer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SurveilEffect(3));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
