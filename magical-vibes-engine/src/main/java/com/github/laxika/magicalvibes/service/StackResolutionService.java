package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;

import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.YouAndOpponentChooseCardNamesOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePrimalClayFormOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.NumberChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

@Slf4j
@Service
public class StackResolutionService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CloneService cloneService;
    private final GraveyardService graveyardService;
    private final LegendRuleService legendRuleService;
    private final StateBasedActionService stateBasedActionService;
    private final GameQueryService gameQueryService;
    private final TargetLegalityService targetLegalityService;
    private final GameLogService gameLogService;
    private final EffectResolutionService effectResolutionService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;
    private final StateTriggerService stateTriggerService;
    private final ExileService exileService;
    private final ParadigmService paradigmService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameMutationCoordinator mutationCoordinator;
    private final CardRevealService cardRevealService;
    private final AuraCopyService auraCopyService;

    public StackResolutionService(BattlefieldEntryService battlefieldEntryService,
                                  CloneService cloneService,
                                  GraveyardService graveyardService,
                                  LegendRuleService legendRuleService,
                                  StateBasedActionService stateBasedActionService,
                                  GameQueryService gameQueryService,
                                  TargetLegalityService targetLegalityService,
                                  GameLogService gameLogService,
                                  EffectResolutionService effectResolutionService,
                                  PlayerInputService playerInputService,
                                  TriggerCollectionService triggerCollectionService,
                                  CreatureControlService creatureControlService,
                                  StateTriggerService stateTriggerService,
                                  ExileService exileService,
                                  GraveyardReturnSupport graveyardReturnSupport,
                                  PermanentCounterSupport permanentCounterSupport,
                                  GameMutationCoordinator mutationCoordinator,
                                  CardRevealService cardRevealService,
                                  AuraCopyService auraCopyService,
                                  @Lazy ParadigmService paradigmService) {
        this.battlefieldEntryService = battlefieldEntryService;
        this.cloneService = cloneService;
        this.graveyardService = graveyardService;
        this.legendRuleService = legendRuleService;
        this.stateBasedActionService = stateBasedActionService;
        this.gameQueryService = gameQueryService;
        this.targetLegalityService = targetLegalityService;
        this.gameLogService = gameLogService;
        this.effectResolutionService = effectResolutionService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.creatureControlService = creatureControlService;
        this.stateTriggerService = stateTriggerService;
        this.exileService = exileService;
        this.graveyardReturnSupport = graveyardReturnSupport;
        this.permanentCounterSupport = permanentCounterSupport;
        this.mutationCoordinator = mutationCoordinator;
        this.cardRevealService = cardRevealService;
        this.auraCopyService = auraCopyService;
        this.paradigmService = paradigmService;
    }

    public void resolveTopOfStack(GameData gameData) {
        if (gameData.stack.isEmpty()) return;

        StackEntry entry = gameData.stack.removeLast();
        gameData.priorityPassedBy.clear();

        // CR 603.8 — clean up state-trigger tracking when the ability leaves the stack
        stateTriggerService.cleanupResolvedStateTrigger(gameData, entry);

        // Track who controls the resolving spell/ability so that causation-sensitive triggers
        // (e.g. Sacred Ground) can tell whether a permanent left the battlefield because of an
        // opponent's spell or ability. Cleared once resolution finishes.
        gameData.currentlyResolvingControllerId = entry.getControllerId();
        try {
            switch (entry.getEntryType()) {
                case CREATURE_SPELL -> resolveCreatureSpell(gameData, entry);
                case ENCHANTMENT_SPELL -> resolveEnchantmentSpell(gameData, entry);
                case ARTIFACT_SPELL -> resolveArtifactSpell(gameData, entry);
                case PLANESWALKER_SPELL -> resolvePlaneswalkerSpell(gameData, entry);
                case BATTLE_SPELL -> resolveBattleSpell(gameData, entry);
                case TRIGGERED_ABILITY, ACTIVATED_ABILITY, SORCERY_SPELL, INSTANT_SPELL ->
                        resolveSpellOrAbility(gameData, entry);
            }
        } finally {
            gameData.currentlyResolvingControllerId = null;
        }

        // Resolution-time may choices are part of the resolving ability, so present them before
        // state-based actions can orphan an Aura that the choice may move.
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // If the ETB handler or a resolution-time may choice already set up a user interaction,
        // skip post-resolution SBA until the choice resolves.
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Check SBA after resolution — creatures may have 0 toughness from effects (e.g. -1/-1)
        stateBasedActionService.performStateBasedActions(gameData);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.PlotTriggerAnyTarget.class)) {
            triggerCollectionService.processNextPlotTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
            triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class)) {
            triggerCollectionService.processNextExploreTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ClashTriggerTarget.class)) {
            triggerCollectionService.processNextClashTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class)) {
            triggerCollectionService.processNextLifeGainTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDrawTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EnteringPermanentAnyTargetTrigger.class)) {
            triggerCollectionService.processNextEnteringPermanentAnyTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterTarget.class)) {
            triggerCollectionService.processNextSagaChapterTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterPlayerTarget.class)) {
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    /** CR 702.146 / siege defeat: while cast transformed, the spell has back-face characteristics. */
    private static Card disturbCharacteristics(StackEntry entry, Card card) {
        if ((entry.isCastWithDisturb() || entry.isCastTransformed()) && card.getBackFaceCard() != null) {
            return card.getBackFaceCard();
        }
        return card;
    }

    /** Permanent enters with front-face identity; Disturb/siege-defeat flips current face to the back. */
    private Permanent createEnteringPermanent(StackEntry entry, Card card, Card characteristics) {
        return createEnteringPermanent(entry, card, characteristics, false);
    }

    private Permanent createEnteringPermanent(StackEntry entry, Card card, Card characteristics,
                                              boolean bestowAsAura) {
        Permanent perm = new Permanent(entry.getBestowOriginalCard() != null
                ? entry.getBestowOriginalCard() : entry.getPhysicalCard());
        if (entry.isCastFaceDown()) {
            perm.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        }
        perm.setCastFromZone(entry.getSourceZone());
        perm.setAlternateCost(entry.isAlternateCost());
        perm.setWebSlingingReturnedCreatureManaValue(entry.getWebSlingingReturnedCreatureManaValue());
        perm.setEnteredFromZone(entry.getSourceZone());
        // CR 707.10: a copy of a spell put onto the stack was never cast, so the permanent it
        // resolves into didn't enter as the result of a cast spell either.
        perm.setCast(!entry.isCopy());
        perm.setManaSpentToCast(entry.getManaSpentToCast());
        // Keywords the spell grants the permanent as it enters (Choreographed Sparks' hasty copy).
        perm.getGrantedKeywords().addAll(entry.getGrantedKeywordsOnEntry());
        // Bloodthirst granted while the spell was on the stack (Bloodlord of Vaasgoth).
        perm.setGrantedBloodthirst(entry.getGrantedBloodthirst());
        entry.getGrantedTriggeredEffectsOnEntry().forEach((slot, effects) ->
                effects.forEach(effect -> perm.addTemporaryTriggeredEffect(slot, effect)));
        // Mirage flash clause: cast at a time a sorcery couldn't have been cast, so its controller
        // sacrifices the permanent it becomes at the beginning of the next cleanup step.
        if (entry.isCastWhenSorceryCouldNotBeCast() && card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(FlashCastWithCleanupSacrificeEffect.class::isInstance)) {
            perm.setSacrificeAtNextCleanup(true);
        }
        if (bestowAsAura) {
            perm.setCard(characteristics);
            perm.setBestow(true);
        } else if (entry.getPhysicalCard() != card) {
            perm.setCard(characteristics);
        } else if ((entry.isCastWithDisturb() || entry.isCastTransformed()) && characteristics != card) {
            perm.setCard(characteristics);
            perm.setTransformed(true);
        }
        if (entry.isCopy() && !perm.getCard().isToken()) {
            Card tokenCard = perm.getCard().createRuntimeCopy();
            tokenCard.setToken(true);
            perm.setCard(tokenCard);
        }
        return perm;
    }

    private void putResolvedPermanentOntoBattlefield(GameData gameData, UUID controllerId,
                                                     Permanent permanent, StackEntry entry) {
        if (entry.isEntersTapped()) {
            permanent.tap();
        }
        permanent.setRepeatedAdditionalCosts(entry.getRepeatedAdditionalCosts());
        if (entry.getRepeatedAdditionalCosts().isEmpty()) {
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, entry.getXValue(), entry.isKicked());
        } else {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent,
                    entry.getXValue(), entry.isKicked(), entry.getRepeatedAdditionalCosts());
        }
    }

    private void handleResolvedPermanentEtb(GameData gameData, UUID controllerId, Card card,
                                            UUID targetId, int etbMode, StackEntry entry) {
        if (entry.getRepeatedAdditionalCosts().isEmpty() && entry.getConvokeCreatureIds().isEmpty()) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card,
                    targetId, true, etbMode, entry.getXValue(), entry.isKicked(), entry.getTargetIds());
        } else if (entry.getRepeatedAdditionalCosts().isEmpty()) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card,
                    targetId, true, etbMode, entry.getXValue(), entry.isKicked(), entry.getTargetIds(),
                    List.of(), entry.getConvokeCreatureIds());
        } else {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card,
                    targetId, true, etbMode, entry.getXValue(), entry.isKicked(), entry.getTargetIds(),
                    entry.getRepeatedAdditionalCosts(), entry.getConvokeCreatureIds());
        }
    }

    private void processResolvedPermanentEtb(GameData gameData, UUID controllerId, Card card,
                                             UUID targetId, StackEntry entry) {
        if (entry.getRepeatedAdditionalCosts().isEmpty() && entry.getConvokeCreatureIds().isEmpty()) {
            battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, card, targetId,
                    true, entry.getXValue(), entry.getXValue(), entry.isKicked(), entry.getTargetIds());
        } else if (entry.getRepeatedAdditionalCosts().isEmpty()) {
            battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, card, targetId,
                    true, entry.getXValue(), entry.getXValue(), entry.isKicked(), entry.getTargetIds(),
                    List.of(), entry.getConvokeCreatureIds());
        } else {
            battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, card, targetId,
                    true, entry.getXValue(), entry.getXValue(), entry.isKicked(), entry.getTargetIds(),
                    entry.getRepeatedAdditionalCosts(), entry.getConvokeCreatureIds());
        }
    }

    /**
     * Flashback/Disturb spells that leave the stack without resolving are exiled
     * (CR 702.33a / back-face exile replacement for Disturb). Siege-defeat casts use normal GY.
     */
    private void disposeFizzledPermanentSpell(GameData gameData, StackEntry entry, Card card) {
        UUID ownerId = entry.getOwnerId();
        Card physicalCard = entry.getPhysicalCard();
        if (entry.isCastWithFlashback() || entry.isCastWithDisturb() || entry.isExileInsteadOfGraveyard()) {
            exileService.exileCard(gameData, ownerId, physicalCard);
        } else {
            graveyardService.addCardToGraveyard(gameData, ownerId, physicalCard);
        }
    }

    /**
     * Starts the "as this permanent enters, choose a card name" choice (CR 614.1c) if {@code card} has
     * one, first granting whatever hand access the effect specifies. Returns {@code false} when there is
     * no such effect, or when the effect restricts the name to opponents' revealed cards and they hold
     * none — in both cases the caller lets the permanent enter right away.
     */
    private boolean beginChooseCardNameOnEnter(GameData gameData, UUID controllerId, Card card) {
        return beginChooseCardNameOnEnter(gameData, controllerId, card, null);
    }

    private boolean beginChooseCardNameOnEnter(
            GameData gameData, UUID controllerId, Card card, UUID attachedTo) {
        ChooseCardNameOnEnterEffect effect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseCardNameOnEnterEffect)
                .map(e -> (ChooseCardNameOnEnterEffect) e)
                .findFirst().orElse(null);
        if (effect == null) {
            return false;
        }
        switch (effect.handAccess()) {
            case LOOK_AT_OPPONENT_HAND -> cardRevealService.lookAtOpponentHand(gameData, controllerId);
            case REVEAL_OPPONENT_HAND -> gameData.playerIds.stream()
                    .filter(playerId -> !playerId.equals(controllerId))
                    .forEach(playerId -> cardRevealService.revealHandToAllPlayers(gameData, playerId));
            case NONE -> {
            }
        }
        boolean restrictToRevealedCards =
                effect.handAccess() == ChooseCardNameOnEnterEffect.HandAccess.REVEAL_OPPONENT_HAND;
        if (effect.nonbasicLandOnly()) {
            return playerInputService.beginCardNameChoice(
                    gameData, controllerId, card, effect.excludedTypes(), restrictToRevealedCards,
                    true, attachedTo);
        }
        return playerInputService.beginCardNameChoice(
                gameData, controllerId, card, effect.excludedTypes(), restrictToRevealedCards,
                false, attachedTo);
    }

    private boolean beginChooseCardTypeOnEnter(GameData gameData, UUID controllerId, Card card) {
        ChooseCardTypeOnEnterEffect effect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseCardTypeOnEnterEffect)
                .map(e -> (ChooseCardTypeOnEnterEffect) e)
                .findFirst().orElse(null);
        if (effect == null) {
            return false;
        }
        if (effect.lookAtOpponentHand()) {
            cardRevealService.lookAtOpponentHand(gameData, controllerId);
        }
        playerInputService.beginCardTypeOnEnterChoice(gameData, controllerId, card, effect.excludedTypes());
        return true;
    }

    private void resolveCreatureSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        Card characteristics = disturbCharacteristics(entry, card);
        UUID controllerId = entry.getControllerId();

        if (!entry.isCastFaceDown() && cloneService.prepareCloneReplacementEffect(
                gameData, controllerId, characteristics, entry.getTargetId(), entry.getXValue(),
                entry.getPhysicalCard(), characteristics != card)) {
            return;
        }

        // "As enters" card name choice (e.g. Meddling Mage) — name must be chosen BEFORE the
        // permanent enters the battlefield (MTG Rule 614.1c)
        if (!entry.isCastFaceDown()
                && (beginChooseCardNameOnEnter(gameData, controllerId, card)
                || beginChooseCardTypeOnEnter(gameData, controllerId, card))) {
            return;
        }

        Permanent perm = createEnteringPermanent(entry, card, characteristics);
        perm.setChosenSubtype(entry.getBeholdChosenSubtype());
        // Carry the zone the spell was cast from so an "if cast from a graveyard, it enters with …
        // counters" as-enters replacement (e.g. Worldheart Phoenix) can gate on it during entry.
        // (castFromZone already set in createEnteringPermanent)

        // Gather Specimens (CR 614.1): if this creature would enter under an opponent's control, it
        // enters under the gatherer's control instead. Resolve up front so the log, ETB triggers, and
        // legend check below all attribute to the effective controller.
        controllerId = battlefieldEntryService.resolveEnteringController(gameData, controllerId, perm);

        // "Enters with … counters" replacement effects (MTG Rule 614.1c) are applied during
        // battlefield entry; pass the spell's cast context (X paid, kicked) along.
        putResolvedPermanentOntoBattlefield(gameData, controllerId, perm, entry);
        if (gameQueryService.findPermanentById(gameData, perm.getId()) == null) {
            return;
        }
        registerBeheldCardReturn(gameData, entry, perm);
        // Carry evoke cast context to the permanent so its evoke sacrifice ETB trigger can gate on it.
        perm.setEvoked(entry.isEvoked());
        // Carry prowl cast context so an "if its prowl cost was paid" ETB trigger can gate on it.
        perm.setProwl(entry.isProwl());
        // Carry spectacle cast context so spectacle-dependent ETB effects can select their branch.
        perm.setSpectacle(entry.isSpectacle());

        // After putPermanentOntoBattlefield, the permanent's card may have been replaced by
        // a copy (e.g. Essence of the Wild). Use the permanent's current card for ETB processing
        // and logging so that the copy's characteristics are used, not the original's.
        Card enteredCard = perm.getCard();

        String playerName = gameData.playerIdToName.get(controllerId);
        if (perm.isFaceDown()) {
            gameLogService.append(gameData, GameLog.text(playerName + " puts a card onto the battlefield face down."));
        } else if (hasEnterWithCountersEffect(enteredCard, CounterType.PLUS_ONE_PLUS_ONE) && perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + " +1/+1 counters", playerName));
        } else if (perm.getCounterCount(CounterType.WISH) > 0) {
            gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.WISH) + " wish counters", playerName));
        } else {
            logEnterBattlefield(gameData, enteredCard, controllerId);
        }

        // "As enters" phylactery counter placement — replacement effect (MTG Rule 614.1c),
        // happens as part of the entering process before state-based actions are checked.
        if (!perm.isFaceDown()) {
            handlePhylacteryCounterPlacement(gameData, controllerId, enteredCard, entry.getTargetId());
            int etbMode = entry.getEtbMode() != null ? entry.getEtbMode() : entry.getXValue();
            handleResolvedPermanentEtb(gameData, controllerId, enteredCard, entry.getTargetId(), etbMode, entry);
        } else {
            battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, enteredCard);
        }
        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void registerBeheldCardReturn(GameData gameData, StackEntry entry, Permanent source) {
        Card beheldCard = entry.getBeheldCard();
        if (beheldCard == null || gameData.findExiledCard(beheldCard.getId()) == null) {
            return;
        }
        UUID ownerId = entry.getBeheldCardOwnerId() != null
                ? entry.getBeheldCardOwnerId() : entry.getControllerId();
        gameData.addExileReturnOnPermanentLeave(source.getId(),
                new PendingExileReturn(beheldCard, ownerId, false, true));
        entry.setBeheldCard(null);
        entry.setBeheldCardOwnerId(null);
    }

    /**
     * Whether the card has an "enters with … counters" replacement effect of the given counter
     * type, bare or wrapped in a {@link ConditionalEffect} ("if kicked", "Raid —"). Used only to
     * pick the entry log message; the counters themselves are applied during battlefield entry.
     */
    private boolean hasEnterWithCountersEffect(Card card, CounterType type) {
        return card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> (e instanceof EnterWithCountersEffect enterWith && enterWith.type() == type)
                        || (e instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof EnterWithCountersEffect wrapped
                        && wrapped.type() == type));
    }

    /**
     * Resolves a reanimation Aura (e.g. Animate Dead): reanimate the enchanted creature card from a
     * graveyard under the Aura's controller and attach the Aura to it. If the enchanted card is no
     * longer a creature card in a graveyard, or is blocked from entering (e.g. Grafdigger's Cage),
     * the Aura is put into its owner's graveyard with nothing to enchant.
     */
    private void resolveReanimationAura(GameData gameData, StackEntry entry, Card card, UUID controllerId) {
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
        if (graveyardCard == null || !graveyardCard.hasType(CardType.CREATURE)) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(card)
                    .text(" fizzles (enchanted creature card no longer in a graveyard).")
                    .build());
            graveyardService.addCardToGraveyard(gameData, entry.getOwnerId(), card);
            log.info("Game {} - {} fizzles, reanimation target {} not in graveyard", gameData.id, card.getName(), entry.getTargetId());
            return;
        }

        // Dance of the Dead: "put … onto the battlefield tapped"; Animate Dead leaves this false.
        boolean enterTapped = card.getEffects(EffectSlot.SPELL).stream()
                .filter(ReturnCardFromGraveyardEffect.class::isInstance)
                .map(ReturnCardFromGraveyardEffect.class::cast)
                .anyMatch(ReturnCardFromGraveyardEffect::enterTapped);
        Permanent creature = graveyardReturnSupport.reanimateTargetedCard(
                gameData, controllerId, graveyardCard, enterTapped);
        if (creature == null) {
            // Blocked from entering (e.g. Grafdigger's Cage): the Aura has nothing to enchant.
            graveyardService.addCardToGraveyard(gameData, entry.getOwnerId(), card);
            log.info("Game {} - {} put into graveyard, reanimated creature could not enter", gameData.id, card.getName());
            return;
        }

        Permanent auraPerm = new Permanent(card);
        auraPerm.setAttachedTo(creature.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, auraPerm);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(card)
                .text(" enters the battlefield attached to ")
                .card(creature.getCard())
                .text(" under " + playerName + "'s control.")
                .build());
        log.info("Game {} - {} reanimates {} for {}", gameData.id, card.getName(), creature.getCard().getName(), playerName);

        triggerCollectionService.checkAuraAttachedTriggers(gameData, auraPerm, creature.getId());
    }

    private void resolveEnchantmentSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();
        // CR 702.146: a spell cast via Disturb has the characteristics of its back face while on the stack.
        Card characteristics = disturbCharacteristics(entry, card);

        if (cloneService.prepareCloneReplacementEffect(gameData, controllerId, card, entry.getTargetId(),
                entry.getXValue())) {
            return;
        }

        // Reanimation Aura that enchants a creature card in a graveyard (e.g. Animate Dead): return
        // the enchanted card to the battlefield under the Aura's controller and attach the Aura to it.
        if (characteristics.isAura() && entry.getTargetZone() == Zone.GRAVEYARD && entry.getTargetId() != null) {
            resolveReanimationAura(gameData, entry, card, controllerId);
            return;
        }

        if (entry.getBestowOriginalCard() != null
                && (entry.getTargetId() == null
                || targetLegalityService.isTargetIllegalOnResolution(gameData, entry))) {
            resolveBestowAsCreature(gameData, entry, controllerId);
            return;
        }

        // Aura that enchants a player (e.g. Curses)
        if (characteristics.isAura() && characteristics.isEnchantPlayer() && entry.getTargetId() != null) {
            UUID targetPlayerId = entry.getTargetId();
            if (!gameData.playerIds.contains(targetPlayerId)) {
                gameLogService.append(gameData, GameLog.builder()
                        .card(characteristics)
                        .text(" fizzles (enchanted player no longer in the game).")
                        .build());
                disposeFizzledPermanentSpell(gameData, entry, card);
                log.info("Game {} - {} fizzles, target player {} no longer in game", gameData.id, characteristics.getName(), targetPlayerId);
            } else if (beginChooseCardNameOnEnter(gameData, controllerId, card, targetPlayerId)) {
                return;
            } else {
                Permanent perm = createEnteringPermanent(entry, card, characteristics);
                perm.setAttachedTo(targetPlayerId);
                putResolvedPermanentOntoBattlefield(gameData, controllerId, perm, entry);

                String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.builder()
                        .card(characteristics)
                        .text(" enters the battlefield attached to " + targetPlayerName + " under " + playerName + "'s control.")
                        .build());
                log.info("Game {} - {} resolves, attached to player {} for {}", gameData.id, characteristics.getName(), targetPlayerName, playerName);
            }
        // Aura fizzles if its target is no longer on the battlefield
        } else if (characteristics.isAura() && entry.getTargetId() != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null) {
                gameLogService.append(gameData, GameLog.builder()
                        .card(characteristics)
                        .text(" fizzles (enchanted creature no longer exists).")
                        .build());
                disposeFizzledPermanentSpell(gameData, entry, card);

                log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, characteristics.getName(), entry.getTargetId());
            } else if (beginChooseCardNameOnEnter(gameData, controllerId, card, entry.getTargetId())) {
                return;
            } else {
                Permanent perm = createEnteringPermanent(
                        entry, card, characteristics, entry.getBestowOriginalCard() != null);
                perm.setAttachedTo(entry.getTargetId());
                putResolvedPermanentOntoBattlefield(gameData, controllerId, perm, entry);

                String playerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.builder()
                        .card(characteristics)
                        .text(" enters the battlefield attached to ")
                        .card(target.getCard())
                        .text(" under " + playerName + "'s control.")
                        .build());
                log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, characteristics.getName(), target.getCard().getName(), playerName);

                triggerCollectionService.checkAuraAttachedTriggers(gameData, perm, target.getId());

                // Handle control-changing auras (e.g., Persuasion): a WHILE_ATTACHED floating
                // layer-2 control effect keyed to the aura permanent
                boolean hasControlEffect = characteristics.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    creatureControlService.applyControlEffect(gameData, controllerId, target,
                            new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                            perm.getId(), characteristics.getName());
                }

                // Check if aura has "as enters" basic land type choice (e.g. Convincing Mirage)
                if (maybeBeginBasicLandTypeChoice(gameData, controllerId, characteristics)) {
                    gameData.interaction.setPendingAuraResolutionEntry(entry);
                }

                // Check if aura has "as enters, choose a color" (e.g. Prismatic Ward)
                ChooseColorEffect auraColorChoice = characteristics.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(e -> e instanceof ChooseColorEffect)
                        .map(e -> (ChooseColorEffect) e)
                        .findFirst().orElse(null);
                if (auraColorChoice != null) {
                    List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                    Permanent justEntered = bf.get(bf.size() - 1);
                    playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), null,
                            auraColorChoice);
                }

                // Check if aura has "as enters, choose a creature" (e.g. Metamorphic Alteration)
                if (!gameData.interaction.isAwaitingInput()) {
                    auraCopyService.beginChooseCreatureOnEnter(gameData, controllerId, perm,
                            characteristics, entry.getTargetId(), true);
                }

                // Process aura ETB effects (e.g., Volition Reins)
                if (!gameData.interaction.isAwaitingInput()) {
                    processResolvedPermanentEtb(gameData, controllerId, characteristics, entry.getTargetId(), entry);
                }
            }
        } else {
            // "As enters" card name choice (e.g. Nevermore) — name must be chosen
            // BEFORE the permanent enters the battlefield (MTG Rule 614.1c)
            if (beginChooseCardNameOnEnter(gameData, controllerId, card)
                    || beginChooseCardTypeOnEnter(gameData, controllerId, card)) {
                return;
            }

            // "As this enters, you and an opponent each choose a card name" (Null Chamber) — both
            // names are chosen before the permanent enters (CR 614.1c); the controller names first.
            boolean dualNameChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .anyMatch(e -> e instanceof YouAndOpponentChooseCardNamesOnEnterEffect);
            if (dualNameChoice) {
                playerInputService.beginDualCardNameChoice(gameData,
                        new ChoiceContext.DualCardNameChoice(card, controllerId, controllerId, null));
                return;
            }

            Permanent enchPerm = createEnteringPermanent(entry, card, characteristics);
            // Pass cast X / kicked so "enters with X counters" replacements and ETB triggers that
            // read XValue (e.g. The Meathook Massacre) see the paid X.
            putResolvedPermanentOntoBattlefield(gameData, controllerId, enchPerm, entry);
            Card enteredCard = enchPerm.getCard();
            logEnterBattlefield(gameData, enteredCard, controllerId);

            // Saga ETB: place first lore counter and trigger chapter I (MTG Rule 714.3a)
            if (enteredCard.isSaga()) {
                int loreCounters = gameQueryService.replaceCounters(gameData, enchPerm,
                        CounterType.LORE, 1, controllerId);
                enchPerm.setCounterCount(CounterType.LORE, loreCounters);
                gameLogService.append(gameData, GameLog.cardThen(enteredCard, " gets a lore counter (1)."));
                log.info("Game {} - {} enters with lore counter 1", gameData.id, enteredCard.getName());
                triggerSagaChapter(gameData, enchPerm, enteredCard, controllerId, 1);
            }

            // Check if enchantment has "as enters" color choice
            ChooseColorEffect enchantmentColorChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .filter(e -> e instanceof ChooseColorEffect)
                    .map(e -> (ChooseColorEffect) e)
                    .findFirst().orElse(null);
            if (enchantmentColorChoice != null) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), null,
                        enchantmentColorChoice);
            }

            // Check if enchantment has "as enters" basic land type choice (e.g. Illusionary Terrain)
            maybeBeginBasicLandTypeChoice(gameData, controllerId, card);

            // Check if enchantment has "as enters" creature type choice (e.g. Xenograft)
            ChooseSubtypeOnEnterEffect subtypeChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .filter(ChooseSubtypeOnEnterEffect.class::isInstance)
                    .map(ChooseSubtypeOnEnterEffect.class::cast)
                    .findFirst()
                    .orElse(null);
            if (subtypeChoice != null) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginSubtypeChoice(gameData, controllerId, justEntered.getId(),
                        subtypeChoice.allowedSubtypes());
            }

            // Check if enchantment has "as enters, choose odd or even" (Ashling's Prerogative)
            boolean needsParityChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .anyMatch(e -> e instanceof ChooseManaValueParityOnEnterEffect);
            if (needsParityChoice) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginManaValueParityChoice(gameData, controllerId, justEntered.getId());
            }

            // Process general ETB effects (e.g., token creation, exile-until-leaves).
            // Pass cast X as etbMode so the ETB stack entry snapshots it for XValue amounts.
            if (!gameData.interaction.isAwaitingInput()) {
                processResolvedPermanentEtb(gameData, controllerId, enteredCard, entry.getTargetId(), entry);
            }

            checkLegendRuleIfIdle(gameData, controllerId);
        }
    }

    private void resolveBestowAsCreature(GameData gameData, StackEntry entry, UUID controllerId) {
        Card card = entry.getBestowOriginalCard();
        if (beginChooseCardNameOnEnter(gameData, controllerId, card)) {
            return;
        }

        Permanent perm = createEnteringPermanent(entry, card, card);
        controllerId = battlefieldEntryService.resolveEnteringController(gameData, controllerId, perm);
        putResolvedPermanentOntoBattlefield(gameData, controllerId, perm, entry);
        Card enteredCard = perm.getCard();
        logEnterBattlefield(gameData, enteredCard, controllerId);
        handleResolvedPermanentEtb(gameData, controllerId, enteredCard, null, entry.getXValue(), entry);
        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void resolveArtifactSpell(GameData gameData, StackEntry entry) {
        if (entry.isCastFaceDown()) {
            resolveCreatureSpell(gameData, entry);
            return;
        }

        Card card = entry.getCard();
        Card characteristics = disturbCharacteristics(entry, card);
        UUID controllerId = entry.getControllerId();

        if (cloneService.prepareCloneReplacementEffect(gameData, controllerId, characteristics, entry.getTargetId(),
                entry.getXValue())) {
            return;
        }

        // "As enters" card name choice (e.g. Pithing Needle, Phyrexian Revoker, Sorcerous Spyglass)
        // — name must be chosen BEFORE the permanent enters the battlefield (MTG Rule 614.1c)
        if (beginChooseCardNameOnEnter(gameData, controllerId, characteristics)) {
            return;
        }

        Permanent perm = createEnteringPermanent(entry, card, characteristics);

        // Gather Specimens (CR 614.1): an artifact creature that would enter under an opponent's
        // control instead enters under the gatherer's control. Resolve up front so the log, ETB
        // triggers, and legend check below all attribute to the effective controller.
        controllerId = battlefieldEntryService.resolveEnteringController(gameData, controllerId, perm);

        // "Enters with … counters" replacement effects (MTG Rule 614.1c) are applied during
        // battlefield entry; pass the spell's cast context (X paid, kicked) along.
        putResolvedPermanentOntoBattlefield(gameData, controllerId, perm, entry);
        // Carry evoke cast context to the permanent so its evoke sacrifice ETB trigger can gate on it.
        perm.setEvoked(entry.isEvoked());
        // Carry prowl cast context so an "if its prowl cost was paid" ETB trigger can gate on it.
        perm.setProwl(entry.isProwl());
        perm.setSpectacle(entry.isSpectacle());

        // After putPermanentOntoBattlefield, the permanent's card may have been replaced by
        // a copy (e.g. Essence of the Wild). Use the permanent's current card for ETB processing
        // and logging so that the copy's characteristics are used, not the original's.
        Card enteredCard = perm.getCard();

        String playerName = gameData.playerIdToName.get(controllerId);
        if (perm.getCounterCount(CounterType.CHARGE) > 0) {
            gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.CHARGE) + " charge counters", playerName));
        } else if (perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + " +1/+1 counters", playerName));
        } else if (perm.getCounterCount(CounterType.WISH) > 0) {
            gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.WISH) + " wish counters", playerName));
        } else {
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
        }

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, enteredCard.getName(), playerName);

        // Check if artifact has "as enters" color choice.
        ChooseColorEffect artifactColorChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseColorEffect)
                .map(e -> (ChooseColorEffect) e)
                .findFirst().orElse(null);
        if (artifactColorChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), null,
                    artifactColorChoice);
        }

        // Check if artifact has "as enters" creature type choice (e.g. Pillar of Origins)
        ChooseSubtypeOnEnterEffect subtypeChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseSubtypeOnEnterEffect.class::isInstance)
                .map(ChooseSubtypeOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (subtypeChoice != null && !gameData.interaction.isAwaitingInput()) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginSubtypeChoice(gameData, controllerId, justEntered.getId(),
                    subtypeChoice.allowedSubtypes());
        }

        // Check if artifact creature has "as this creature enters, it becomes your choice of ..."
        // shape choice (Primal Clay)
        boolean needsPrimalClayFormChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChoosePrimalClayFormOnEnterEffect);
        if (needsPrimalClayFormChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginPrimalClayFormChoice(gameData, controllerId, justEntered.getId());
        }

        // Check if artifact has "as this enters, choose a number between X and Y" (Shapeshifter)
        NumberChoiceEffect numberChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof NumberChoiceEffect)
                .map(e -> (NumberChoiceEffect) e)
                .findFirst().orElse(null);
        if (numberChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginNumberChoice(gameData, controllerId, justEntered.getId(),
                    numberChoice.minNumber(), numberChoice.maxNumber());
        }

        // Process ETB effects for all artifacts (creature and non-creature)
        if (!gameData.interaction.isAwaitingInput()) {
            int etbMode = entry.getEtbMode() != null ? entry.getEtbMode() : entry.getXValue();
            handleResolvedPermanentEtb(gameData, controllerId, enteredCard, entry.getTargetId(), etbMode, entry);
        }

        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void resolvePlaneswalkerSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        Permanent perm = new Permanent(card);
        // Planeswalkers with printed loyalty "X" (e.g. Nissa, Steward of Elements) enter with
        // loyalty counters equal to the X paid for their {X} cost. Scryfall's non-numeric "X"
        // loyalty parses to 0, so an {X} in the mana cost is the reliable signal.
        int startingLoyalty = card.getLoyalty() != null ? card.getLoyalty() : 0;
        if (card.getParsedManaCost() != null && card.getParsedManaCost().hasX()) {
            startingLoyalty = entry.getXValue();
        }
        startingLoyalty += entry.getGrantedAdditionalLoyaltyCounters();
        if (card.getKeywords().contains(Keyword.COMPLEATED)) {
            startingLoyalty -= 2 * entry.getPhyrexianManaPaidWithLife();
        }
        startingLoyalty = gameQueryService.replaceCounters(gameData, perm, controllerId,
                CounterType.LOYALTY, startingLoyalty, controllerId);
        perm.setCounterCount(CounterType.LOYALTY, startingLoyalty);
        perm.setSummoningSick(false);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
        permanentCounterSupport.fireLoyaltyCountersPutOnControlledPlaneswalkersTriggers(
                gameData, controllerId, startingLoyalty);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                card, perm.getCounterCount(CounterType.LOYALTY) + " loyalty", playerName));

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
        if (!gameData.interaction.isAwaitingInput()) {
            handleResolvedPermanentEtb(gameData, controllerId, perm.getCard(), entry.getTargetId(), entry.getXValue(), entry);
        }
        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void resolveBattleSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        Permanent perm = new Permanent(card);
        int startingDefense = card.getDefense() != null ? card.getDefense() : 0;
        startingDefense = gameQueryService.replaceCounters(gameData, perm, controllerId,
                CounterType.DEFENSE, startingDefense, controllerId);
        perm.setCounterCount(CounterType.DEFENSE, startingDefense);
        perm.setSummoningSick(false);
        // Siege: as this battle enters, choose an opponent to protect it (2p: the only opponent).
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        perm.setProtectorPlayerId(opponentId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.entersBattlefieldWithUnder(
                card, perm.getCounterCount(CounterType.DEFENSE) + " defense", playerName));

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);

        // Process ETB (e.g. Invasion of Innistrad's -13/-13)
        if (!gameData.interaction.isAwaitingInput()) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, card, entry.getTargetId(), true, entry.getXValue(),
                    entry.isKicked(), entry.getTargetIds());
        }
        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void resolveSpellOrAbility(GameData gameData, StackEntry entry) {
        // Check if targeted spell/ability fizzles due to illegal target
        boolean targetFizzled = entry.getEffectsToResolve().stream()
                .noneMatch(CardEffect::resolvesWhenTargetIllegal)
                && targetLegalityService.isTargetIllegalOnResolution(gameData, entry);

        if (targetFizzled) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" fizzles (illegal target).")
                    .build());
            log.info("Game {} - {} fizzles, target {} is illegal",
                    gameData.id, entry.getDescription(), entry.getTargetId());

            // Fizzled spells still go to graveyard (copies cease to exist per rule 707.10a)
            // Flashback spells are exiled instead (CR 702.33a)
            if (isNonCopySpell(entry)) {
                if (entry.isCastWithFlashback() || entry.isExileInsteadOfGraveyard()) {
                    exileService.exileCard(gameData, entry.getOwnerId(), entry.getCard());
                    gameLogService.append(gameData, GameLog.isExiled(entry.getCard()));
                } else {
                    graveyardService.addCardToGraveyard(gameData, entry.getOwnerId(), entry.getCard());
                }
            }
        } else {
            gameLogService.append(gameData, GameLog.resolves(entry.getCard(), entry.getDescription()));
            log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

            countAbilityResolution(gameData, entry);
            if (entry.isExileAndReturnToHandAtNextEndStep()) {
                entry.setExileInsteadOfGraveyard(true);
            }
            effectResolutionService.resolveEffects(gameData, entry);

            // A spell that pauses for input must remain undisposed until its effects finish.
            if (gameData.pendingEffectResolutionEntry != null) {
                return;
            }

            // Rule 723.1b: "End the turn" exiles the resolving spell itself (copies cease to exist per rule 707.10a)
            if (gameData.endTurnRequested) {
                gameData.endTurnRequested = false;
                if (isNonCopySpell(entry)) {
                    exileService.exileCard(gameData, entry.getOwnerId(), entry.getCard());
                }
                return;
            }

            handleSpellDisposition(gameData, entry);
        }

        // Only clear cast-time mana snapshots when resolution finished synchronously. If it paused
        // for player input (pendingEffectResolutionEntry set), the snapshots must survive until the
        // resumed resolution drains — EffectResolutionService clears them at that point. Clearing
        // now would break a ColorSpentToCast condition re-checked on a "you may" resume.
        if (entry.getCard() != null && gameData.pendingEffectResolutionEntry == null) {
            gameData.clearSpellCastConvergeValue(entry.getCard().getId());
            gameData.clearSpellCastColorsSpent(entry.getCard().getId());
            gameData.clearSpellCastManaSpentByColor(entry.getCard().getId());
            gameData.clearSpellCastSnowManaSpent(entry.getCard().getId());
            gameData.clearSpellCastSnowManaSpentByColor(entry.getCard().getId());
            gameData.clearSpellCastManaSpentOnX(entry.getCard().getId());
        }
    }

    public void completeDeferredAuraResolution(GameData gameData, StackEntry entry) {
        Card characteristics = disturbCharacteristics(entry, entry.getCard());
        battlefieldEntryService.processCreatureETBEffects(gameData, entry.getControllerId(), characteristics,
                entry.getTargetId(), true, entry.getTargetIds());
        handleSpellDisposition(gameData, entry);
    }

    /** Completes disposition for a spell whose effect resolution resumed after player input. */
    public void completeDeferredSpellResolution(GameData gameData, StackEntry entry) {
        handleSpellDisposition(gameData, entry);
    }

    /**
     * Counts this resolution in {@code GameData.permanentAbilityResolutionsThisTurn} when the
     * entry is an activated ability whose effects branch on {@code NthAbilityResolutionThisTurn}
     * ("if this is the Nth time this ability has resolved this turn", e.g. Ashling the Pilgrim).
     * Counted at resolution (not activation), so copies of the ability count but activations
     * countered on the stack do not; fizzled abilities never reach this point. Incremented before
     * effect dispatch so the condition sees the count including the current resolution, and only
     * here (not on async resume) so each resolution counts exactly once.
     */
    private void countAbilityResolution(GameData gameData, StackEntry entry) {
        if (entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY || entry.getSourcePermanentId() == null) {
            return;
        }
        boolean countsResolutions = entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof NthAbilityResolutionThisTurn);
        if (countsResolutions) {
            gameData.permanentAbilityResolutionsThisTurn.merge(entry.getSourcePermanentId(), 1, Integer::sum);
        }
    }

    /**
     * Determines where a resolved spell card ends up: hand, exile, library, or graveyard.
     * Copies cease to exist per rule 707.10a and abilities have no card to dispose of.
     */
    private void handleSpellDisposition(GameData gameData, StackEntry entry) {
        if (!isNonCopySpell(entry)) {
            return;
        }

        // A spell leaves for its OWNER's zone (graveyard / hand / library), which equals the controller
        // for every normal cast; it only diverges when the spell was cast by a non-owner (Sen Triplets),
        // where entry.getOwnerId() carries the true owner so the card returns to their zones.
        UUID ownerId = entry.getOwnerId();
        Card physicalCard = entry.getPhysicalCard();
        boolean plotOnResolution = gameData.spellsWithPlotOnResolution.remove(physicalCard.getId());

        // Feather's replacement is chosen first when it is available; otherwise flashback's
        // replacement exiles the spell instead of letting it go anywhere else.
        if (entry.isExileAndReturnToHandAtNextEndStep()
                && !entry.isReturnToHandAfterResolving()
                && entry.getPutIntoLibraryPositionAfterResolving() == null
                && gameData.pendingReturnToHandOnDiscardType == null) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            gameData.addToExile(ownerId, physicalCard);
            gameData.queueDelayedAction(new ReturnExiledCardToHandAtNextEndStep(
                    physicalCard.getId(), ownerId));
            gameLogService.append(gameData, GameLog.isExiled(entry.getCard()));
        } else if (entry.isCastWithFlashback()) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            gameData.addToExile(ownerId, physicalCard);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is exiled (flashback)."));
        } else if (entry.isCastWithOmen()) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            gameData.playerDecks.get(ownerId).add(physicalCard);
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is shuffled into its owner's library."));
        } else if (entry.isReturnToHandAfterResolving()) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            gameData.addCardToHand(ownerId, physicalCard);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is returned to its owner's hand."));
        } else if (entry.getPutIntoLibraryPositionAfterResolving() != null) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            // Approach of the Second Sun: the resolved spell goes into its owner's library N from the top.
            List<Card> deck = gameData.playerDecks.get(ownerId);
            int position = Math.min(entry.getPutIntoLibraryPositionAfterResolving(), deck.size());
            deck.add(position, physicalCard);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " is put " + (position + 1) + " from the top of its owner's library."));
        } else if (gameData.pendingReturnToHandOnDiscardType != null) {
            // Spell disposition deferred — will be resolved after the async discard
            // completes (e.g. Psychic Miasma: goes to hand if a land is discarded,
            // otherwise to graveyard).
        } else if (entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof ExileSpellEffect)) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            gameData.addToExile(ownerId, physicalCard);
            gameLogService.append(gameData, GameLog.isExiled(entry.getCard()));
        } else if (entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect)) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            // Ensure the card is shuffled into library even when an earlier effect
            // required user input and broke the effect resolution loop before
            // the ShuffleIntoLibraryEffect handler could run.
            List<Card> deck = gameData.playerDecks.get(ownerId);
            if (!deck.contains(physicalCard)) {
                deck.add(physicalCard);
                LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is shuffled into its owner's library."));
            }
        } else if (entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof PutSelfOnBottomOfOwnersLibraryEffect)) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(physicalCard);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is put on the bottom of its owner's library."));
        } else if (entry.getCard().getKeywords().contains(Keyword.PARADIGM)) {
            gameData.spellsWithDreamCounterOnResolution.remove(entry.getCard().getId());
            paradigmService.onParadigmSpellResolved(gameData, entry);
        } else if (entry.getSourceZone() == Zone.HAND
                && (entry.getCard().getKeywords().contains(Keyword.REBOUND)
                || gameQueryService.hasSpellCastingAbilityGrant(
                gameData, entry.getControllerId(), entry.getCard(), Keyword.REBOUND))) {
            gameData.spellsWithDreamCounterOnResolution.remove(entry.getCard().getId());
            gameData.addToExile(ownerId, entry.getCard());
            gameData.queueDelayedAction(new ReboundAtNextUpkeep(
                    entry.getControllerId(), ownerId, entry.getCard()));
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is exiled with rebound."));
        } else if (entry.isExileInsteadOfGraveyard()) {
            gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId());
            gameData.addToExile(ownerId, physicalCard);
            gameLogService.append(gameData, GameLog.isExiled(entry.getCard()));
        } else if (plotOnResolution) {
            exileService.exileCard(gameData, ownerId, physicalCard);
            gameData.plottedCardIds.add(physicalCard.getId());
            gameData.exilePlayPermissions.put(physicalCard.getId(), ownerId);
            gameData.exilePlayWithoutPayingManaCost.add(physicalCard.getId());
            triggerCollectionService.checkPlotTriggers(gameData, ownerId, physicalCard);
            gameLogService.append(gameData, GameLog.cardThen(physicalCard, " becomes plotted."));
        } else if (gameData.spellsWithDreamCounterOnResolution.remove(physicalCard.getId())) {
            gameData.addToExile(ownerId, physicalCard);
            gameData.exiledCardDreamCounters.put(physicalCard.getId(), 1);
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " is exiled with a dream counter."));
        } else {
            graveyardService.addCardToGraveyard(gameData, ownerId, physicalCard);
        }
    }

    private void handlePhylacteryCounterPlacement(GameData gameData, UUID controllerId, Card card, UUID targetId) {
        boolean hasPhylacteryEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof PutPhylacteryCounterOnTargetPermanentEffect);
        if (!hasPhylacteryEffect) return;

        // Per MTG rulings: "If you control no artifacts as Phylactery Lich enters the
        // battlefield, its ability does nothing." No target was chosen — skip placement.
        if (targetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        // Validate the chosen permanent is an artifact controlled by the caster.
        // This does NOT use targeting (shroud/hexproof don't prevent it per MTG rulings).
        UUID targetController = gameQueryService.findPermanentController(gameData, targetId);
        if (!controllerId.equals(targetController)) return;
        if (!gameQueryService.isArtifact(gameData, target)) return;
        if (gameQueryService.cantHaveCounters(gameData, target)) return;

        int placed = gameQueryService.replaceCounters(gameData, target, CounterType.PHYLACTERY, 1,
                controllerId);
        if (placed <= 0) return;
        target.setCounterCount(CounterType.PHYLACTERY, target.getCounterCount(CounterType.PHYLACTERY) + placed);
        triggerCollectionService.checkYouPutCountersTriggers(gameData, controllerId, placed);
        gameLogService.append(gameData,
                GameLog.cardTextCard(card, " puts a phylactery counter on ", target.getCard(), "."));
        log.info("Game {} - {} puts a phylactery counter on {}", gameData.id, card.getName(), target.getCard().getName());
    }

    private void logEnterBattlefield(GameData gameData, Card card, UUID controllerId) {
        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, playerName));
        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
    }

    /**
     * Triggers the appropriate Saga chapter ability for the given lore counter value.
     * If the chapter's effects need targeting, queues for target selection;
     * otherwise pushes the chapter's effects onto the stack as a triggered ability.
     */
    private void triggerSagaChapter(GameData gameData, Permanent sagaPerm, Card card, UUID controllerId, int loreCount) {
        EffectSlot chapterSlot = switch (loreCount) {
            case 1 -> EffectSlot.SAGA_CHAPTER_I;
            case 2 -> EffectSlot.SAGA_CHAPTER_II;
            case 3 -> EffectSlot.SAGA_CHAPTER_III;
            default -> null;
        };
        if (chapterSlot == null) return;

        List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
        if (chapterEffects.isEmpty()) return;

        String chapterName = switch (loreCount) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(loreCount);
        };

        boolean needsPlayerTarget = chapterEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                || card.getSagaChapterTargetFilters(chapterSlot).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter.class::isInstance);
        boolean hasSagaTargetGroups = !card.getSagaChapterTargetGroups(chapterSlot).isEmpty();
        boolean needsPermanentTarget = chapterEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                || hasSagaTargetGroups;
        boolean needsGraveyardTarget = chapterEffects.stream().anyMatch(e ->
                e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                        || e instanceof ReturnTargetCardsFromGraveyardToHandEffect);
        if (hasSagaTargetGroups) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPerm.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot),
                            card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
            gameLogService.append(gameData, GameLog.cardThen(card,
                    "'s chapter " + chapterName + " ability triggers."));
            log.info("Game {} - {} chapter {} triggers (awaiting grouped target selection)",
                    gameData.id, card.getName(), chapterName);
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsPlayerTarget && needsPermanentTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    card, controllerId, new ArrayList<>(chapterEffects), false,
                    sagaChapterAnyTargetFilter(chapterEffects), 0, sagaPerm.getId()));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
            log.info("Game {} - {} chapter {} triggers (awaiting any target selection)",
                    gameData.id, card.getName(), chapterName);
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        } else if (needsPlayerTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterPlayerTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPerm.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot)));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
            log.info("Game {} - {} chapter {} triggers (awaiting player target selection)", gameData.id, card.getName(), chapterName);
            triggerCollectionService.processNextSagaChapterPlayerTarget(gameData);
        } else if (needsPermanentTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPerm.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot),
                            card.getSagaChapterTargetGroups(chapterSlot), List.of(), 0));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
            log.info("Game {} - {} chapter {} triggers (awaiting target selection)", gameData.id, card.getName(), chapterName);
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsGraveyardTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterGraveyardTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPerm.getId(), chapterName));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
            log.info("Game {} - {} chapter {} triggers (awaiting graveyard target selection)", gameData.id, card.getName(), chapterName);
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s chapter " + chapterName + " ability",
                    new ArrayList<>(chapterEffects),
                    null,
                    sagaPerm.getId()
            ));

            gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
            log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
        }
    }

    private TargetFilter sagaChapterAnyTargetFilter(List<CardEffect> chapterEffects) {
        CardEffect permanentTargetEffect = chapterEffects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .findFirst()
                .orElseThrow();
        var permanentPredicate = permanentTargetEffect.targetSpec().targetPredicate()
                .permanentRestriction().orElse(new PermanentTruePredicate());
        PlayerRelation relation = chapterEffects.stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                .map(CardEffect::targetPlayerRelation)
                .findFirst()
                .orElse(PlayerRelation.ANY);
        return new AnyTargetPredicateTargetFilter(permanentPredicate,
                new PlayerRelationPredicate(relation), "target opponent or planeswalker");
    }

    private boolean maybeBeginBasicLandTypeChoice(GameData gameData, UUID controllerId, Card characteristics) {
        ChooseBasicLandTypeOnEnterEffect choose = characteristics.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseBasicLandTypeOnEnterEffect)
                .map(e -> (ChooseBasicLandTypeOnEnterEffect) e)
                .findFirst()
                .orElse(null);
        if (choose == null) {
            return false;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        Permanent justEntered = bf.get(bf.size() - 1);
        playerInputService.beginBasicLandTypeChoice(
                gameData, controllerId, justEntered.getId(), false, choose.choicesRequired() > 1,
                choose.allowedTypes());
        return true;
    }

    private void checkLegendRuleIfIdle(GameData gameData, UUID controllerId) {
        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private static boolean isNonCopySpell(StackEntry entry) {
        return (entry.getEntryType() == StackEntryType.SORCERY_SPELL
                || entry.getEntryType() == StackEntryType.INSTANT_SPELL)
                && !entry.isCopy();
    }

}


