package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect;
import com.github.laxika.magicalvibes.model.effect.ResolvePendingExileReturnEffect;

import java.util.List;

/**
 * The permanent moves the delayed triggered abilities that wait for a step will make when it begins ("exile them at
 * the beginning of the next end step", "destroy it at end of combat"): one for each step of those abilities.
 */
public final class DelayedPermanentMoves {

    private DelayedPermanentMoves() {
    }

    /** Every permanent move a waiting delayed ability will make, in the order the abilities were made. */
    public static List<DelayedPermanentAction> waiting(GameData gameData) {
        return gameData.temporaryGlobalTriggeredAbilities.stream()
                .flatMap(delayed -> delayed.ability().effects().stream())
                .map(registration -> registration.effect())
                .filter(ResolveDelayedPermanentActionEffect.class::isInstance)
                .map(effect -> ((ResolveDelayedPermanentActionEffect) effect).action())
                .toList();
    }

    /** The waiting permanent moves of {@code kind}. */
    public static List<DelayedPermanentAction> waiting(GameData gameData, DelayedPermanentActionKind kind) {
        return waiting(gameData).stream().filter(action -> action.kind() == kind).toList();
    }

    /** The returns of exiled cards the waiting delayed abilities will make, in the order the abilities were made. */
    public static List<PendingExileReturn> waitingExileReturns(GameData gameData) {
        return gameData.temporaryGlobalTriggeredAbilities.stream()
                .flatMap(delayed -> delayed.ability().effects().stream())
                .map(registration -> registration.effect())
                .filter(ResolvePendingExileReturnEffect.class::isInstance)
                .map(effect -> ((ResolvePendingExileReturnEffect) effect).pending())
                .toList();
    }

    /** The waiting delayed abilities that make at least one permanent move. */
    public static List<TemporaryGlobalTriggeredAbility> abilities(GameData gameData) {
        return gameData.temporaryGlobalTriggeredAbilities.stream()
                .filter(delayed -> delayed.ability().effects().stream()
                        .anyMatch(registration -> registration.effect() instanceof ResolveDelayedPermanentActionEffect))
                .toList();
    }
}
