package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.UUID;

/**
 * The delayed "add mana at the beginning of your next main phase" ability whose amount a resolving spell or ability
 * fixes as it resolves (that spell's mana value, the mana spent to cast it, the mana a drained land would have made).
 */
final class DelayedManaSupport {

    private DelayedManaSupport() {
    }

    /**
     * The delayed triggered ability "at the beginning of your next main phase, add {@code amount} mana" a resolving
     * spell or ability of {@code card} controlled by {@code controllerId} made: of {@code color}, or in any combination
     * of colors when {@code anyColorCombination}, and when {@code optional} only if its controller means to. It is
     * about the next main phase of either kind, or only the first (precombat) one when {@code firstMainOnly} ("your
     * next first main phase"), and ends with the turn when {@code thisTurnOnly} ("your next main phase this turn").
     */
    public static TemporaryGlobalTriggeredAbility delayedMana(UUID controllerId, Card card, UUID sourcePermanentId,
                                                              ManaColor color, int amount, boolean optional,
                                                              boolean thisTurnOnly, boolean anyColorCombination,
                                                              boolean firstMainOnly) {
        CardEffect manaEffect = anyColorCombination
                ? new AwardManaOfColorsEffect(ManaColor.COLORS, amount)
                : new AwardManaEffect(color, amount);
        if (optional) {
            manaEffect = new MayEffect(manaEffect, "Add " + amount + " " + color.getCode() + "?");
        }
        List<TriggerCondition> mainPhases = firstMainOnly
                ? List.of(new TriggerCondition(Triggers.precombatMainPhase(ActivePlayer.YOU)))
                : List.of(new TriggerCondition(Triggers.precombatMainPhase(ActivePlayer.YOU)),
                new TriggerCondition(Triggers.postcombatMainPhase(ActivePlayer.YOU)));
        TriggeredAbility ability = new TriggeredAbility(mainPhases, TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(manaEffect)));
        return thisTurnOnly
                ? TemporaryGlobalTriggeredAbility.nextThisTurn(controllerId, card, ability, sourcePermanentId)
                : TemporaryGlobalTriggeredAbility.atNextStep(controllerId, card, ability, sourcePermanentId, null,
                null, DelayedReferents.NONE);
    }
}
