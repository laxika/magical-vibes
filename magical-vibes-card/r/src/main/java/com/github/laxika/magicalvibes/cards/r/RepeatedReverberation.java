package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "156")
public class RepeatedReverberation extends Card {

    public RepeatedReverberation() {
        addSpellEffect(RegisterGlobalTriggeredAbilityEffect.nextThisTurn(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))))),
                new TriggerCondition(Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.LOYALTY))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new CopyTriggeringSpellOrAbilityEffect().withCopies(2))))));
    }
}
