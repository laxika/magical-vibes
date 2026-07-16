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
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePrimalClayFormOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;
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
    private final GameBroadcastService gameBroadcastService;
    private final EffectResolutionService effectResolutionService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;
    private final StateTriggerService stateTriggerService;
    private final ExileService exileService;
    private final ParadigmService paradigmService;

    public StackResolutionService(BattlefieldEntryService battlefieldEntryService,
                                  CloneService cloneService,
                                  GraveyardService graveyardService,
                                  LegendRuleService legendRuleService,
                                  StateBasedActionService stateBasedActionService,
                                  GameQueryService gameQueryService,
                                  TargetLegalityService targetLegalityService,
                                  GameBroadcastService gameBroadcastService,
                                  EffectResolutionService effectResolutionService,
                                  PlayerInputService playerInputService,
                                  TriggerCollectionService triggerCollectionService,
                                  CreatureControlService creatureControlService,
                                  StateTriggerService stateTriggerService,
                                  ExileService exileService,
                                  @Lazy ParadigmService paradigmService) {
        this.battlefieldEntryService = battlefieldEntryService;
        this.cloneService = cloneService;
        this.graveyardService = graveyardService;
        this.legendRuleService = legendRuleService;
        this.stateBasedActionService = stateBasedActionService;
        this.gameQueryService = gameQueryService;
        this.targetLegalityService = targetLegalityService;
        this.gameBroadcastService = gameBroadcastService;
        this.effectResolutionService = effectResolutionService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.creatureControlService = creatureControlService;
        this.stateTriggerService = stateTriggerService;
        this.exileService = exileService;
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
                case TRIGGERED_ABILITY, ACTIVATED_ABILITY, SORCERY_SPELL, INSTANT_SPELL ->
                        resolveSpellOrAbility(gameData, entry);
            }
        } finally {
            gameData.currentlyResolvingControllerId = null;
        }

        // If the ETB handler already set up a user interaction (e.g. Clone copy choice),
        // skip post-resolution SBA — the creature must remain alive until the choice resolves.
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Check SBA after resolution — creatures may have 0 toughness from effects (e.g. -1/-1)
        stateBasedActionService.performStateBasedActions(gameData);

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

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfLeavesTriggerTarget.class)) {
            triggerCollectionService.processNextSelfLeavesTriggerTarget(gameData);
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

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EntersFromGraveyardTriggerTarget.class)) {
            triggerCollectionService.processNextEntersFromGraveyardTriggerTarget(gameData);
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

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameBroadcastService.broadcastGameState(gameData);
    }

    private void resolveCreatureSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        if (cloneService.prepareCloneReplacementEffect(gameData, controllerId, card, entry.getTargetId())) {
            return;
        }

        Permanent perm = new Permanent(card);

        // "Enters with … counters" replacement effects (MTG Rule 614.1c) are applied during
        // battlefield entry; pass the spell's cast context (X paid, kicked) along.
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm,
                entry.getXValue(), entry.isKicked());
        // Carry evoke cast context to the permanent so its evoke sacrifice ETB trigger can gate on it.
        perm.setEvoked(entry.isEvoked());
        // Carry prowl cast context so an "if its prowl cost was paid" ETB trigger can gate on it.
        perm.setProwl(entry.isProwl());

        // After putPermanentOntoBattlefield, the permanent's card may have been replaced by
        // a copy (e.g. Essence of the Wild). Use the permanent's current card for ETB processing
        // and logging so that the copy's characteristics are used, not the original's.
        Card enteredCard = perm.getCard();

        String playerName = gameData.playerIdToName.get(controllerId);
        if (hasEnterWithCountersEffect(enteredCard, CounterType.PLUS_ONE_PLUS_ONE) && perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            String logEntry = enteredCard.getName() + " enters the battlefield with " + perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + " +1/+1 counters under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        } else if (perm.getCounterCount(CounterType.WISH) > 0) {
            String logEntry = enteredCard.getName() + " enters the battlefield with " + perm.getCounterCount(CounterType.WISH) + " wish counters under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        } else {
            logEnterBattlefield(gameData, enteredCard, controllerId);
        }

        // "As enters" phylactery counter placement — replacement effect (MTG Rule 614.1c),
        // happens as part of the entering process before state-based actions are checked.
        handlePhylacteryCounterPlacement(gameData, controllerId, enteredCard, entry.getTargetId());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, enteredCard, entry.getTargetId(), true, entry.getXValue(), entry.isKicked(), entry.getTargetIds());
        checkLegendRuleIfIdle(gameData, controllerId);
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

    private void resolveEnchantmentSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        // Aura that enchants a player (e.g. Curses)
        if (card.isAura() && card.isEnchantPlayer() && entry.getTargetId() != null) {
            UUID targetPlayerId = entry.getTargetId();
            if (!gameData.playerIds.contains(targetPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .card(card)
                        .text(" fizzles (enchanted player no longer in the game).")
                        .build());
                graveyardService.addCardToGraveyard(gameData, controllerId, card);
                log.info("Game {} - {} fizzles, target player {} no longer in game", gameData.id, card.getName(), targetPlayerId);
            } else {
                Permanent perm = new Permanent(card);
                perm.setAttachedTo(targetPlayerId);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

                String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .card(card)
                        .text(" enters the battlefield attached to " + targetPlayerName + " under " + playerName + "'s control.")
                        .build());
                log.info("Game {} - {} resolves, attached to player {} for {}", gameData.id, card.getName(), targetPlayerName, playerName);
            }
        // Aura fizzles if its target is no longer on the battlefield
        } else if (card.isAura() && entry.getTargetId() != null) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .card(card)
                        .text(" fizzles (enchanted creature no longer exists).")
                        .build());
                graveyardService.addCardToGraveyard(gameData, controllerId, card);

                log.info("Game {} - {} fizzles, target {} no longer exists", gameData.id, card.getName(), entry.getTargetId());
            } else {
                Permanent perm = new Permanent(card);
                perm.setAttachedTo(entry.getTargetId());
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .card(card)
                        .text(" enters the battlefield attached to ")
                        .card(target.getCard())
                        .text(" under " + playerName + "'s control.")
                        .build());
                log.info("Game {} - {} resolves, attached to {} for {}", gameData.id, card.getName(), target.getCard().getName(), playerName);

                // Handle control-changing auras (e.g., Persuasion): a WHILE_ATTACHED floating
                // layer-2 control effect keyed to the aura permanent
                boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    creatureControlService.applyControlEffect(gameData, controllerId, target,
                            new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                            perm.getId(), card.getName());
                }

                // Check if aura has "as enters" basic land type choice (e.g. Convincing Mirage)
                boolean needsBasicLandTypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .anyMatch(e -> e instanceof ChooseBasicLandTypeOnEnterEffect);
                if (needsBasicLandTypeChoice) {
                    List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                    Permanent justEntered = bf.get(bf.size() - 1);
                    playerInputService.beginBasicLandTypeChoice(gameData, controllerId, justEntered.getId());
                }

                // Process aura ETB effects (e.g., Volition Reins)
                if (!gameData.interaction.isAwaitingInput()) {
                    battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, card, entry.getTargetId(), true, entry.getTargetIds());
                }
            }
        } else {
            // "As enters" card name choice (e.g. Nevermore) — name must be chosen
            // BEFORE the permanent enters the battlefield (MTG Rule 614.1c)
            var chooseNameEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .filter(e -> e instanceof ChooseCardNameOnEnterEffect)
                    .map(e -> (ChooseCardNameOnEnterEffect) e)
                    .findFirst().orElse(null);
            if (chooseNameEffect != null) {
                if (chooseNameEffect.lookAtOpponentHand()) {
                    gameBroadcastService.revealOpponentHandToPlayer(gameData, controllerId);
                }
                playerInputService.beginCardNameChoice(gameData, controllerId, card, chooseNameEffect.excludedTypes());
                return;
            }

            Permanent enchPerm = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, enchPerm);
            logEnterBattlefield(gameData, card, controllerId);

            // Saga ETB: place first lore counter and trigger chapter I (MTG Rule 714.3a)
            if (card.isSaga()) {
                enchPerm.setCounterCount(CounterType.LORE, 1);
                String counterLog = card.getName() + " gets a lore counter (1).";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(counterLog));
                log.info("Game {} - {} enters with lore counter 1", gameData.id, card.getName());
                triggerSagaChapter(gameData, enchPerm, card, controllerId, 1);
            }

            // Check if enchantment has "as enters" color choice
            boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .anyMatch(e -> e instanceof ChooseColorEffect);
            if (needsColorChoice) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), null);
            }

            // Check if enchantment has "as enters" creature type choice (e.g. Xenograft)
            boolean needsSubtypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .anyMatch(e -> e instanceof ChooseSubtypeOnEnterEffect);
            if (needsSubtypeChoice) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginSubtypeChoice(gameData, controllerId, justEntered.getId());
            }

            // Check if enchantment has "as enters, choose odd or even" (Ashling's Prerogative)
            boolean needsParityChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                    .anyMatch(e -> e instanceof ChooseManaValueParityOnEnterEffect);
            if (needsParityChoice) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                playerInputService.beginManaValueParityChoice(gameData, controllerId, justEntered.getId());
            }

            // Process general ETB effects (e.g., token creation, exile-until-leaves)
            if (!gameData.interaction.isAwaitingInput()) {
                battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, card, entry.getTargetId(), true, entry.getTargetIds());
            }

            checkLegendRuleIfIdle(gameData, controllerId);
        }
    }

    private void resolveArtifactSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        if (cloneService.prepareCloneReplacementEffect(gameData, controllerId, card, entry.getTargetId())) {
            return;
        }

        // "As enters" card name choice (e.g. Pithing Needle, Phyrexian Revoker, Sorcerous Spyglass)
        // — name must be chosen BEFORE the permanent enters the battlefield (MTG Rule 614.1c)
        var chooseNameEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseCardNameOnEnterEffect)
                .map(e -> (ChooseCardNameOnEnterEffect) e)
                .findFirst().orElse(null);
        if (chooseNameEffect != null) {
            if (chooseNameEffect.lookAtOpponentHand()) {
                gameBroadcastService.revealOpponentHandToPlayer(gameData, controllerId);
            }
            playerInputService.beginCardNameChoice(gameData, controllerId, card, chooseNameEffect.excludedTypes());
            return;
        }

        Permanent perm = new Permanent(card);

        // "Enters with … counters" replacement effects (MTG Rule 614.1c) are applied during
        // battlefield entry; pass the spell's cast context (X paid, kicked) along.
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm,
                entry.getXValue(), entry.isKicked());
        // Carry evoke cast context to the permanent so its evoke sacrifice ETB trigger can gate on it.
        perm.setEvoked(entry.isEvoked());
        // Carry prowl cast context so an "if its prowl cost was paid" ETB trigger can gate on it.
        perm.setProwl(entry.isProwl());

        // After putPermanentOntoBattlefield, the permanent's card may have been replaced by
        // a copy (e.g. Essence of the Wild). Use the permanent's current card for ETB processing
        // and logging so that the copy's characteristics are used, not the original's.
        Card enteredCard = perm.getCard();

        String playerName = gameData.playerIdToName.get(controllerId);
        if (perm.getCounterCount(CounterType.CHARGE) > 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.CHARGE) + " charge counters", playerName));
        } else if (perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + " +1/+1 counters", playerName));
        } else if (perm.getCounterCount(CounterType.WISH) > 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldWithUnder(
                    enteredCard, perm.getCounterCount(CounterType.WISH) + " wish counters", playerName));
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
        }

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, enteredCard.getName(), playerName);

        // Check if artifact has "as enters" creature type choice (e.g. Pillar of Origins)
        boolean needsSubtypeChoice = enteredCard.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseSubtypeOnEnterEffect);
        if (needsSubtypeChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginSubtypeChoice(gameData, controllerId, justEntered.getId());
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

        // Process ETB effects for all artifacts (creature and non-creature)
        if (!gameData.interaction.isAwaitingInput()) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, enteredCard, entry.getTargetId(), true, entry.getXValue(), entry.isKicked(), entry.getTargetIds());
        }

        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void resolvePlaneswalkerSpell(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, card.getLoyalty() != null ? card.getLoyalty() : 0);
        perm.setSummoningSick(false);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = card.getName() + " enters the battlefield with " + perm.getCounterCount(CounterType.LOYALTY) + " loyalty under " + playerName + "'s control.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, card.getName(), playerName);
        checkLegendRuleIfIdle(gameData, controllerId);
    }

    private void resolveSpellOrAbility(GameData gameData, StackEntry entry) {
        // Check if targeted spell/ability fizzles due to illegal target
        boolean targetFizzled = targetLegalityService.isTargetIllegalOnResolution(gameData, entry);

        if (targetFizzled) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" fizzles (illegal target).")
                    .build());
            log.info("Game {} - {} fizzles, target {} is illegal",
                    gameData.id, entry.getDescription(), entry.getTargetId());

            // Fizzled spells still go to graveyard (copies cease to exist per rule 707.10a)
            // Flashback spells are exiled instead (CR 702.33a)
            if (isNonCopySpell(entry)) {
                if (entry.isCastWithFlashback()) {
                    exileService.exileCard(gameData, entry.getControllerId(), entry.getCard());
                    String exileLog = entry.getCard().getName() + " is exiled (flashback).";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
                } else {
                    graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), entry.getCard());
                }
            }
        } else {
            String logEntry = entry.getDescription() + " resolves.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} resolves", gameData.id, entry.getDescription());

            countAbilityResolution(gameData, entry);
            effectResolutionService.resolveEffects(gameData, entry);

            // Rule 723.1b: "End the turn" exiles the resolving spell itself (copies cease to exist per rule 707.10a)
            if (gameData.endTurnRequested) {
                gameData.endTurnRequested = false;
                if (isNonCopySpell(entry)) {
                    exileService.exileCard(gameData, entry.getControllerId(), entry.getCard());
                }
                gameBroadcastService.broadcastGameState(gameData);
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
        }
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

        // CR 702.33a: "If the flashback cost was paid, exile this card instead of
        // putting it anywhere else any time it would leave the stack." This overrides
        // return-to-hand, shuffle-into-library, and all other disposition effects.
        if (entry.isCastWithFlashback()) {
            gameData.addToExile(entry.getControllerId(), entry.getCard());
            String exileLog = entry.getCard().getName() + " is exiled (flashback).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
        } else if (entry.isReturnToHandAfterResolving()) {
            gameData.addCardToHand(entry.getControllerId(), entry.getCard());
            String returnLog = entry.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(returnLog));
        } else if (gameData.pendingReturnToHandOnDiscardType != null) {
            // Spell disposition deferred — will be resolved after the async discard
            // completes (e.g. Psychic Miasma: goes to hand if a land is discarded,
            // otherwise to graveyard).
        } else if (entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof ExileSpellEffect)) {
            gameData.addToExile(entry.getControllerId(), entry.getCard());
            String exileLog = entry.getCard().getName() + " is exiled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
        } else if (entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof ShuffleIntoLibraryEffect)) {
            // Ensure the card is shuffled into library even when an earlier effect
            // required user input and broke the effect resolution loop before
            // the ShuffleIntoLibraryEffect handler could run.
            List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
            if (!deck.contains(entry.getCard())) {
                deck.add(entry.getCard());
                LibraryShuffleHelper.shuffleLibrary(gameData, entry.getControllerId());
                String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(shuffleLog));
            }
        } else if (entry.getEffectsToResolve().stream()
                .anyMatch(e -> e instanceof PutSelfOnBottomOfOwnersLibraryEffect)) {
            List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
            deck.add(entry.getCard());
            String bottomLog = entry.getCard().getName() + " is put on the bottom of its owner's library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(bottomLog));
        } else if (entry.getCard().getKeywords().contains(Keyword.PARADIGM)) {
            paradigmService.onParadigmSpellResolved(gameData, entry);
        } else {
            graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), entry.getCard());
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

        target.setCounterCount(CounterType.PHYLACTERY, target.getCounterCount(CounterType.PHYLACTERY) + 1);
        String logEntry = card.getName() + " puts a phylactery counter on " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} puts a phylactery counter on {}", gameData.id, card.getName(), target.getCard().getName());
    }

    private void logEnterBattlefield(GameData gameData, Card card, UUID controllerId) {
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(card, playerName));
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

        boolean needsPermanentTarget = chapterEffects.stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
        boolean needsGraveyardTarget = chapterEffects.stream().anyMatch(e -> e.targetSpec().category().isGraveyard());
        if (needsPermanentTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPerm.getId(), chapterName,
                            card.getSagaChapterTargetFilters(chapterSlot)));
            String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chapter {} triggers (awaiting target selection)", gameData.id, card.getName(), chapterName);
            triggerCollectionService.processNextSagaChapterTarget(gameData);
        } else if (needsGraveyardTarget) {
            gameData.queueInteraction(
                    new PermanentChoiceContext.SagaChapterGraveyardTarget(card, controllerId,
                            new ArrayList<>(chapterEffects), sagaPerm.getId(), chapterName));
            String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

            String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
        }
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


