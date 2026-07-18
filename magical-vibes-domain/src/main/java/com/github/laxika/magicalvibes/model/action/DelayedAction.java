package com.github.laxika.magicalvibes.model.action;

/**
 * Marker sealed interface for "do X later at timing point Y" delayed actions that live on
 * {@code GameData.delayedActions}. Each permitted record is an immutable payload scheduled by an
 * effect and drained at a fixed timing point (end of combat, end step, a specific turn step, or
 * turn cleanup).
 *
 * <p>Mirrors the {@code PendingInteraction} design (stage 1): a near-marker sealed interface with a
 * single unified queue on {@code GameData} plus type-filtered helpers. Deliberately carries no
 * {@code timing()} method — the relative servicing order of the families at a shared timing point is
 * encoded at the drain call sites (see {@code CombatService} / {@code StepTriggerService} /
 * {@code TurnProgressionService}), not on the records, exactly as {@code PendingInteraction} keeps
 * its cross-kind order at the call sites. A {@code timing()} enum would be dead code no consumer
 * dispatches on.
 *
 * <p>Each family is its own record file in this package, keeping them out of the already-large
 * {@code GameData} god-class.
 */
public sealed interface DelayedAction permits
        DelayedPermanentAction,
        SacrificeAtEndOfCombat,
        DestroyEquipmentAtEndOfCombat,
        PutMinusOneCounterAtEndOfCombat,
        PutCounterOnPermanentAtEndOfCombat,
        RemoveCounterFromSourceAtEndOfCombat,
        GainControlOfPermanentAtEndOfCombat,
        ExileAndReturnTransformedAtEndOfCombat,
        DestroyNonAttackersAtEndStep,
        LoseGameAtEndStep,
        DelayedPlusOneCounters,
        DelayedPlusZeroPlusOneCounters,
        DelayedUntapPermanents,
        DelayedCreateToken,
        DelayedGraveyardToHandReturn,
        ReturnExiledCardToHandAtEndStep,
        DelayedGraveyardToBattlefieldTransformedReturn,
        DelayedGraveyardToBattlefieldUnderControl,
        DelayedCombatDamageLoot,
        DelayedCombatDamageReflection,
        AddManaAtNextMainPhase,
        LoseLifeAtNextDrawStepUnlessPays,
        DrawCardsAtNextUpkeep,
        ExileToOwnerGraveyardAtNextUpkeep,
        RevokeExilePlayPermissionAtNextUpkeep,
        PendingExileReturn {
}
