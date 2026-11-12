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
        DelayedZoneChangeTrigger,
        DelayedCleanupTrigger,
        DelayedAdditionalCombatBeginningEffect,
        TargetCreatureMustAttackNextCombat,
        DelayedBeginningOfCombatTrigger,
        DelayedEndOfCombatTrigger,
        DelayedEndStepTrigger,
        DelayedDrawStepTriggerWhileSourceTapped,
        DelayedSourceUntapsOrLeavesTrigger,
        DelayedStillExiledCardsEndStepTrigger,
        DelayedEndStepIfStillInHandTrigger,
        DelayedPermanentAction,
        DestroyChosenCreaturesIfDidNotAttackAtEndStep,
        ExilePermanentAtControllerEndStep,
        SacrificeAtEndOfCombat,
        TapCombatOpponentsAtEndOfCombat,
        DealDamageToPermanentAtEndOfCombat,
        DestroyCombatOpponentsAtEndOfCombat,
        RemoveCounterFromSourceAtEndOfCombat,
        DestroyNonAttackersAtEndStep,
        DestroyPermanentIfDidNotAttackAtEndStep,
        DelayedDestroyAllPermanents,
        DelayedChooseOpponentGainsControlOfSource,
        ExileToOwnerGraveyardAtNextEndStep,
        LoseGameAtEndStep,
        DelayedPlusOneCounters,
        DelayedPlusZeroPlusOneCounters,
        PutCounterOnPermanentAtNextEndStep,
        RemoveCounterFromPermanentAtNextEndStep,
        RemoveCountersFromPermanentAtNextCleanup,
        DelayedUntapPermanents,
        DelayedCreateToken,
        DelayedCreateTokenAtNextUpkeep,
        DelayedCreateTokenCopy,
        DelayedRevealCreatureCardsToBattlefield,
        DelayedExileCreatedPermanentsAtEndStep,
        DelayedNontokenAttackTokenCreation,
        DelayedAttackTokenCreation,
        DelayedAttackDamage,
        DelayedAttackUntap,
        DelayedLoseLifeAndReturnFromGraveyard,
        DelayedGraveyardToHandReturn,
        DiscardCardsAtNextEndStep,
        DiscardCardsAtNextTurnEndStep,
        DamageForCardsStillExiledAtNextEndStep,
        ExileCardsFromOwnGraveyardAtNextEndStep,
        ReturnExiledCardToHandAtEndStep,
        ReturnExiledCardToHandAtNextEndStep,
        DelayedGraveyardToBattlefieldSelfReturn,
        DelayedGraveyardToBattlefieldTransformedReturn,
        DelayedGraveyardToBattlefieldUnderControl,
        DelayedGraveyardCardsToBattlefieldUnderControl,
        DelayedControllerEndStepGraveyardReturn,
        DelayedControllerEndStepTrigger,
        DelayedReturnAuraAttachedToPermanent,
        DelayedReturnCurseAttachedToPlayer,
        DelayedReturnSourceAuraToCreature,
        DelayedCombatDamageEffect,
        DelayedCombatDamageDraw,
        DelayedCombatDamageLookAtHandAndDraw,
        DelayedCombatDamageLoot,
        DelayedCombatDamageToken,
        DelayedCombatDamageBecomeMonarch,
        DelayedCombatDamageReflection,
        DelayedBlockerBoost,
        DelayedBlockOrBecomesBlockedTrigger,
        DelayedAttackerBoost,
        DelayedAttackerKeywordGrant,
        DelayedOpponentAttackerBoost,
        DelayedWatchedCreatureAttack,
        DelayedDestroyCreatureDealingCombatDamageToPlaneswalker,
        DelayedWatchedCreaturesCombatDamage,
        DelayedNamedCreatureCombatDamage,
        DelayedWatchedCreatureDealsDamage,
        DelayedWatchedCreatureDealtDamage,
        DelayedControllerSpellCastTrigger,
        EpicDelayedTrigger,
        DelayedUnblockedAttackerPowerDamage,
        DelayedUnblockedAttackerGainLife,
        DelayedUnblockedAttackerCubeCounter,
        DelayedUnblockedAttackerUntapRemoveFromCombat,
        DelayedAttackerDeclarationControl,
        DelayedBlockerDeclarationControl,
        DelayedCamouflage,
        DelayedDestroyCreatureDamagedByWatchedCreature,
        DelayedWatchedCreatureDealtDamageByAttackingCreature,
        DelayedSacrificeSourceWhenTargetLeaves,
        DelayedSacrificeTargetWhenSourceLeaves,
        DelayedDestroyTargetWhenSourceLeaves,
        DiscardSpecificCardAtNextEndStep,
        DelayedExileReturnCounterTrigger,
        DelayedSacrificeTargetPermanentAtEndStep,
        DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost,
        DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast,
        DelayedCoinFlipSacrificeTargetPermanentAtEndStep,
        AddManaAtNextMainPhase,
        DrawCardsAtNextMainPhase,
        EchoAtNextUpkeep,
        LoseLifeAtNextDrawStepUnlessPays,
        DamageAtNextUpkeepUnlessPays,
        PoisonAtNextUpkeepUnlessPays,
        PayManaOrLoseGameAtNextUpkeep,
        DrawCardsAtNextUpkeep,
        RandomDiscardCardsAtNextUpkeep,
        DrawCardsAtNextEndStep,
        SacrificeSelfAtNextEndStepTrigger,
        SacrificePermanentAtControllerEndStepUnlessPays,
        ExileToOwnerGraveyardAtNextUpkeep,
        ExilePermanentAtNextUpkeep,
        GrantExilePlayPermissionAtNextTurn,
        PutCounterOnPermanentAtNextUpkeep,
        RevokeExilePlayPermissionAtNextUpkeep,
        TransformSourceAtNextUpkeep,
        GrantChosenLandwalkAtNextUpkeep,
        CyclopeanTombUpkeepCleanup,
        ReboundAtNextUpkeep,
        DimensionalBreachUpkeepReturn,
        PendingExileReturn,
        ReturnExiledCardAtNextEndStepUnlessPays,
        EachPlayerHandExileReturnAtNextEndStep,
        TargetPlayerHandExileReturnAtNextTurnEndStep,
        ExpireControlAtEndOfNextTurn,
        PhasedOutUntilEndOfNextTurn,
        DelayedDamageDoubling,
        DelayedControllerDamageMultiplication,
        DelayedSourceDamageMultiplication,
        UnattachEquipmentAtNextEndStep {

    /**
     * The identity of the resolution that scheduled this action ({@code GameData.resolutionOrdinal}), for the
     * actions whose delayed triggered abilities are grouped into one stack object per resolution (CR 603.7a); 0 for
     * an action that has not been queued yet, or that is never grouped.
     */
    default long schedulingResolution() {
        return 0;
    }

    /**
     * This action as scheduled by the resolution {@code schedulingResolution}; {@code GameData.queueDelayedAction}
     * stamps every action with it. An action that is never grouped is returned as it is.
     */
    default DelayedAction scheduledBy(long schedulingResolution) {
        return this;
    }
}
