package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "76")
public class TreacherousWerewolf extends Card {

    public TreacherousWerewolf() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(threshold,
                new GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(4)), GrantScope.SELF)));
    }
}
