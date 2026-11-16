package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Creates the delayed triggered ability "When you next cast a spell with {X} in its mana cost or activate an ability
 * with {X} in its activation cost this turn, copy that spell or ability. You may choose new targets for the copy. (A
 * copy of a permanent spell becomes a token.)" (Magus Lucea Kane, {@link #delayedAbility}): one ability with both
 * trigger events, so whichever happens first is the next time it triggers and uses it up (CR 603.7b).
 */
public record CopyNextSpellOrAbilityWithXThisTurnEffect() implements CardEffect {

    /** The delayed triggered ability this creates. */
    public TriggeredAbility delayedAbility() {
        return new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.youCastSpell().withFilter(new CardHasXInManaCostPredicate())),
                new TriggerCondition(Triggers.youActivateAbility()
                        .withKind(AbilityTrigger.AbilityKind.WITH_X_IN_COST))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new CopyTriggeringSpellOrAbilityEffect().permanentSpellCopiesAreTokens())));
    }
}
