package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "74")
public class MintharaOfTheAbsolute extends Card {

    private static final String CARD_NAME = "Minthara of the Absolute";

    public MintharaOfTheAbsolute() {
        addStaticEffect(new DynamicStaticBoostEffect(new SourceIntensity(), new Fixed(0), GrantScope.ALL_OWN_CREATURES));
        // One ability (CR 603.3) whose limit covers both conditions: this permanent, and another you control.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)),
                new TriggerCondition(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(new IntensifyNamedCardsEffect(CARD_NAME)))));
    }
}
