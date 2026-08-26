package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "JUD", collectorNumber = "76")
public class TreacherousWerewolf extends Card {

    public TreacherousWerewolf() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantTriggeredAbilityEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(4), GrantScope.SELF)));
    }
}
