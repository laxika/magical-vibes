package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Creates the delayed triggered ability "When you next cast an instant spell, cast a sorcery spell, or activate a
 * loyalty ability this turn, copy that spell or ability twice. You may choose new targets for the copies."
 * ({@link #delayedAbility}): one ability with both trigger events, so whichever happens first is the next time it
 * triggers and uses it up (CR 603.7b).
 */
public record CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect() implements CardEffect {

    /** The delayed triggered ability this creates. */
    public TriggeredAbility delayedAbility() {
        return new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))))),
                new TriggerCondition(Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.LOYALTY))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new CopyTriggeringSpellOrAbilityEffect().withCopies(2))));
    }
}
