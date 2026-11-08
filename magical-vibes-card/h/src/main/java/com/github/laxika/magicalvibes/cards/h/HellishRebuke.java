package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIs;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "26")
public class HellishRebuke extends Card {

    public HellishRebuke() {
        addSpellEffect(new GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect(
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsDamageToPlayer(),
                                TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIs(null)),
                                SequenceEffect.of(new SacrificeSelfEffect(), new LoseLifeEffect(2))),
                        GrantScope.SELF)));
    }
}
