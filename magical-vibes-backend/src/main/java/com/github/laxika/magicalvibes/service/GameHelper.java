package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.WarpWorldAuraChoiceRequest;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessFewLandsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GameHelper {

    private final SessionManager sessionManager;
    private final GameRegistry gameRegistry;
    private final CardViewFactory cardViewFactory;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final LegendRuleService legendRuleService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final DraftRegistry draftRegistry;
    private final DraftService draftService;
    private final CreatureControlService creatureControlService;
    // @Lazy to break circular dependency: TriggerCollectionService → GameHelper → TriggerCollectionService
    private TriggerCollectionService triggerCollectionService;

    public GameHelper(SessionManager sessionManager,
                      GameRegistry gameRegistry,
                      CardViewFactory cardViewFactory,
                      GameQueryService gameQueryService,
                      GameBroadcastService gameBroadcastService,
                      PlayerInputService playerInputService,
                      LegendRuleService legendRuleService,
                      TriggeredAbilityQueueService triggeredAbilityQueueService,
                      DraftRegistry draftRegistry,
                      DraftService draftService,
                      CreatureControlService creatureControlService,
                      @Lazy TriggerCollectionService triggerCollectionService) {
        this.sessionManager = sessionManager;
        this.gameRegistry = gameRegistry;
        this.cardViewFactory = cardViewFactory;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.playerInputService = playerInputService;
        this.legendRuleService = legendRuleService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.draftRegistry = draftRegistry;
        this.draftService = draftService;
        this.creatureControlService = creatureControlService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Sets the TriggerCollectionService for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    // ===== General utility =====

    public void setImprintedCardOnPermanent(GameData gameData, UUID sourcePermanentId, Card card) {
        Permanent perm = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (perm != null) {
            perm.getCard().setImprintedCard(card);
        }
    }

    // ===== Lifecycle methods =====

    public void resolveMillPlayer(GameData gameData, UUID targetPlayerId, int count) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsToMill = Math.min(count, deck.size());
        List<Card> milledCards = new ArrayList<>(deck.subList(0, cardsToMill));
        deck.subList(0, cardsToMill).clear();
        for (Card card : milledCards) {
            addCardToGraveyard(gameData, targetPlayerId, card);
        }
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
    }

    /**
     * Adds a card to its owner's graveyard, or applies a replacement effect (e.g. shuffle into library).
     * Returns true if the card was actually put into the graveyard, false if a replacement effect was applied.
     * Callers should skip "dies" / graveyard triggers when this returns false (CR 614.6).
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card) {
        return addCardToGraveyard(gameData, ownerId, card, null);
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        if (card.isShufflesIntoLibraryFromGraveyard()) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            Collections.shuffle(deck);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        } else {
            gameData.playerGraveyards.get(ownerId).add(card);
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone);
            return true;
        }
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent) {
        Set<CardType> enterTappedTypes = snapshotEnterTappedTypes(gameData);
        applyEnterTappedEffects(permanent, enterTappedTypes);
        applySelfEnterTapped(permanent);
        applyConditionalEnterTapped(gameData, controllerId, permanent);
        gameData.playerBattlefields.get(controllerId).add(permanent);
        gameData.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, k -> new ArrayList<>())
                .add(permanent.getCard());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes) {
        applyEnterTappedEffects(permanent, enterTappedTypes);
        applySelfEnterTapped(permanent);
        applyConditionalEnterTapped(gameData, controllerId, permanent);
        gameData.playerBattlefields.get(controllerId).add(permanent);
        gameData.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, k -> new ArrayList<>())
                .add(permanent.getCard());
    }

    public Set<CardType> snapshotEnterTappedTypes(GameData gameData) {
        Set<CardType> enterTappedTypes = EnumSet.noneOf(CardType.class);

        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof EnterPermanentsOfTypesTappedEffect enterTapped)) {
                    continue;
                }
                enterTappedTypes.addAll(enterTapped.cardTypes());
            }
        });
        return enterTappedTypes;
    }

    private void applyEnterTappedEffects(Permanent enteringPermanent, Set<CardType> enterTappedTypes) {
        if (enterTappedTypes == null || enterTappedTypes.isEmpty()) {
            return;
        }
        if (matchesAnyType(enteringPermanent.getCard(), enterTappedTypes)) {
            enteringPermanent.tap();
        }
    }

    private void applySelfEnterTapped(Permanent enteringPermanent) {
        if (enteringPermanent.getCard().isEntersTapped()) {
            enteringPermanent.tap();
        }
    }

    private void applyConditionalEnterTapped(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        for (CardEffect effect : enteringPermanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof EntersTappedUnlessFewLandsEffect fewLands) {
                // Count other lands the controller already has on the battlefield
                List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                int otherLandCount = 0;
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard().getType() == CardType.LAND
                                || p.getCard().getAdditionalTypes().contains(CardType.LAND)) {
                            otherLandCount++;
                        }
                    }
                }
                if (otherLandCount > fewLands.maxOtherLands()) {
                    enteringPermanent.tap();
                }
            }
        }
    }

    private boolean matchesAnyType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) {
            return true;
        }
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) {
                return true;
            }
        }
        return false;
    }

    private void updateThisTurnBattlefieldToGraveyardTracking(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        if (sourceZone == Zone.BATTLEFIELD
                && !card.isToken()
                && (card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE))) {
            tracked.add(card.getId());
            triggerDamagedCreatureDiesAbilities(gameData, card.getId());
        } else {
            tracked.remove(card.getId());
        }
    }

    public void recordCreatureDamagedByPermanent(GameData gameData, UUID sourcePermanentId, Permanent damagedCreature, int damage) {
        if (sourcePermanentId == null || damagedCreature == null || damage <= 0) {
            return;
        }
        if (!gameQueryService.isCreature(gameData, damagedCreature)) {
            return;
        }

        gameData.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(damagedCreature.getCard().getId());
    }

    private void triggerDamagedCreatureDiesAbilities(GameData gameData, UUID dyingCreatureCardId) {
        if (dyingCreatureCardId == null) {
            return;
        }

        for (Map.Entry<UUID, Set<UUID>> entry : gameData.creatureCardsDamagedThisTurnBySourcePermanent.entrySet()) {
            UUID sourcePermanentId = entry.getKey();
            Set<UUID> damagedCreatureIds = entry.getValue();
            if (!damagedCreatureIds.contains(dyingCreatureCardId)) {
                continue;
            }

            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source == null) {
                continue;
            }

            UUID controllerId = findPermanentController(gameData, sourcePermanentId);
            if (controllerId == null) {
                continue;
            }

            List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        sourcePermanentId
                ));
                String triggerLog = source.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (damaged creature died this turn)", gameData.id, source.getCard().getName());
            }
        }

        for (Set<UUID> damagedCreatureIds : gameData.creatureCardsDamagedThisTurnBySourcePermanent.values()) {
            damagedCreatureIds.remove(dyingCreatureCardId);
        }
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    private UUID findPermanentController(GameData gameData, UUID permanentId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) {
                    return playerId;
                }
            }
        }
        return null;
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, null);
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature, Permanent dyingPermanent) {
        List<CardEffect> deathEffects = dyingCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects.isEmpty()) return;

        for (CardEffect effect : deathEffects) {
            if (effect instanceof DealDamageToBlockedAttackersOnDeathEffect deathDmg) {
                // Only triggers during combat and if the creature was blocking
                TurnStep step = gameData.currentStep;
                if (dyingPermanent != null && step != null
                        && step.ordinal() >= TurnStep.BEGINNING_OF_COMBAT.ordinal()
                        && step.ordinal() <= TurnStep.END_OF_COMBAT.ordinal()
                        && !dyingPermanent.getBlockingTargetPermanentIds().isEmpty()) {
                    List<UUID> targetIds = new ArrayList<>(dyingPermanent.getBlockingTargetPermanentIds());
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            dyingCard,
                            controllerId,
                            dyingCard.getName() + "'s ability",
                            new ArrayList<>(List.of(deathDmg)),
                            0,
                            targetIds
                    ));
                }
            } else if (effect instanceof MayPayManaEffect mayPay) {
                gameData.queueMayAbility(dyingCard, controllerId, mayPay, null);
            } else if (effect instanceof MayEffect may) {
                gameData.queueMayAbility(dyingCard, controllerId, may);
            } else if (effect.canTargetPermanent() || effect.canTargetPlayer()) {
                // Targeted death trigger — queue for target selection after current action completes
                gameData.pendingDeathTriggerTargets.add(new PermanentChoiceContext.DeathTriggerTarget(
                        dyingCard, controllerId, new ArrayList<>(List.of(effect))
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        dyingCard,
                        controllerId,
                        dyingCard.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
            }
        }
    }

    public void beginNextPendingLibraryBottomReorder(GameData gameData) {
        LibraryBottomReorderRequest request = gameData.pendingLibraryBottomReorders.pollFirst();
        if (request == null) {
            return;
        }

        UUID playerId = request.playerId();
        List<Card> cards = request.cards();
        if (cards == null || cards.size() <= 1) {
            if (cards != null && cards.size() == 1) {
                gameData.playerDecks.get(playerId).add(cards.getFirst());
            }
            beginNextPendingLibraryBottomReorder(gameData);
            return;
        }

        gameData.interaction.beginLibraryReorder(playerId, cards, true);

        List<CardView> cardViews = cards.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(playerId, new ReorderLibraryCardsMessage(
                cardViews,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."
        ));

        String logMsg = gameData.playerIdToName.get(playerId) + " orders cards for the bottom of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
    }

    public void beginNextPendingWarpWorldAuraChoice(GameData gameData) {
        WarpWorldAuraChoiceRequest request = gameData.warpWorldOperation.pendingAuraChoices.pollFirst();
        if (request == null) {
            return;
        }

        gameData.interaction.setPendingAuraCard(request.auraCard());
        playerInputService.beginPermanentChoice(
                gameData,
                request.controllerId(),
                request.validTargetIds(),
                "Choose a permanent for " + request.auraCard().getName() + " to enchant."
        );
    }

    public void placePendingWarpWorldEnchantments(GameData gameData) {
        Set<CardType> enterTappedTypes = gameData.warpWorldOperation.enterTappedTypesSnapshot;
        for (WarpWorldEnchantmentPlacement placement : gameData.warpWorldOperation.pendingEnchantmentPlacements) {
            UUID controllerId = placement.controllerId();
            Card card = placement.card();
            Permanent permanent = new Permanent(card);
            if (placement.attachmentTargetId() != null) {
                permanent.setAttachedTo(placement.attachmentTargetId());
            }
            putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

            if (placement.attachmentTargetId() != null) {
                boolean hasControlEffect = card.getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
                if (hasControlEffect) {
                    Permanent target = gameQueryService.findPermanentById(gameData, placement.attachmentTargetId());
                    if (target != null) {
                        creatureControlService.stealPermanent(gameData, controllerId, target);
                    }
                }
            }
        }
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
    }

    public void finalizePendingWarpWorld(GameData gameData) {
        if (gameData.warpWorldOperation.sourceName == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> creatures = gameData.warpWorldOperation.pendingCreaturesByPlayer.getOrDefault(playerId, List.of());
            for (Card card : creatures) {
                processCreatureETBEffects(gameData, playerId, card, null, false);
            }
        }

        if (!gameData.interaction.isAwaitingInput() && gameData.warpWorldOperation.needsLegendChecks) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                legendRuleService.checkLegendRule(gameData, playerId);
            }
        }

        gameBroadcastService.logAndBroadcast(gameData,
                gameData.warpWorldOperation.sourceName + " shuffles all permanents into libraries and warps the world.");

        gameData.warpWorldOperation.pendingCreaturesByPlayer.clear();
        gameData.warpWorldOperation.pendingAuraChoices.clear();
        gameData.warpWorldOperation.pendingEnchantmentPlacements.clear();
        gameData.warpWorldOperation.enterTappedTypesSnapshot.clear();
        gameData.warpWorldOperation.needsLegendChecks = false;
        gameData.warpWorldOperation.sourceName = null;
    }

    public boolean checkWinCondition(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            int poison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            if (life <= 0 || poison >= 10) {
                // Check if the player is protected from losing (e.g. Platinum Angel)
                if (!gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                    continue;
                }

                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry;
                if (poison >= 10) {
                    logEntry = gameData.playerIdToName.get(playerId) + " has 10 poison counters and loses! " + winnerName + " wins!";
                } else {
                    logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                }
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

                notifyDraftIfTournamentGame(gameData, winnerId);

                gameRegistry.remove(gameData.id);

                log.info("Game {} - {} wins! {} is at {} life, {} poison", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life, poison);
                return true;
            }
        }
        return false;
    }

    public void declareWinner(GameData gameData, UUID winnerId) {
        String winnerName = gameData.playerIdToName.get(winnerId);

        gameData.status = GameStatus.FINISHED;

        String logEntry = winnerName + " wins the game!";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

        notifyDraftIfTournamentGame(gameData, winnerId);

        gameRegistry.remove(gameData.id);

        log.info("Game {} - {} wins!", gameData.id, winnerName);
    }

    private void notifyDraftIfTournamentGame(GameData gameData, UUID winnerId) {
        if (gameData.draftId != null) {
            DraftData draftData = draftRegistry.get(gameData.draftId);
            if (draftData != null) {
                draftService.handleGameFinished(draftData, winnerId);
            }
        }
    }

    void resetEndOfTurnModifiers(GameData gameData) {
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0 || !p.getGrantedKeywords().isEmpty()
                    || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0 || p.isCantBeBlocked()
                    || p.isAnimatedUntilEndOfTurn() || p.isCantRegenerateThisTurn()
                    || p.isExileInsteadOfDieThisTurn() || !p.getGrantedCardTypes().isEmpty()) {
                p.resetModifiers();
                p.setDamagePreventionShield(0);
                p.setRegenerationShield(0);
            }
        });

        gameData.playerDamagePreventionShields.clear();
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.preventDamageFromColors.clear();
        gameData.combatDamageRedirectTarget = null;
        gameData.playerColorDamagePreventionCount.clear();
        gameData.playerSourceDamagePreventionIds.clear();
        gameData.permanentsPreventedFromDealingDamage.clear();
        gameData.drawReplacementTargetToController.clear();
    }

    void drainManaPools(GameData gameData) {
        // Check if any permanent on the battlefield prevents mana drain (e.g. Upwelling)
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PreventManaDrainEffect.class::isInstance)) {
                    return;
                }
            }
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
            }
        }
    }

    boolean hasNoMaximumHandSize(GameData gameData, UUID playerId) {
        if (gameData.playersWithNoMaximumHandSize.contains(playerId)) {
            return true;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    // ===== Prevention methods =====

    int applyGlobalPreventionShield(GameData gameData, int damage) {
        int shield = gameData.globalDamagePreventionShield;
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.globalDamagePreventionShield = shield - prevented;
        return damage - prevented;
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof PreventAllDamageEffect)) return 0;
        if (gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
        if (damage > 0 && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof PreventDamageAndAddMinusCountersEffect)) {
            if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() + damage);
            }
            return 0;
        }
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = permanent.getDamagePreventionShield();
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        permanent.setDamagePreventionShield(shield - prevented);
        return damage - prevented;
    }

    public int applyPlayerPreventionShield(GameData gameData, UUID playerId, int damage) {
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.playerDamagePreventionShields.put(playerId, shield - prevented);
        return damage - prevented;
    }

    public boolean isSourceDamagePreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        if (sourcePermanentId == null) return false;
        Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(playerId);
        return preventedSources != null && preventedSources.contains(sourcePermanentId);
    }

    public boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor) {
        if (sourceColor == null) return false;
        Map<CardColor, Integer> colorMap = gameData.playerColorDamagePreventionCount.get(playerId);
        if (colorMap == null) return false;
        Integer count = colorMap.get(sourceColor);
        if (count == null || count <= 0) return false;
        colorMap.put(sourceColor, count - 1);
        return true;
    }

    // ===== Mana =====

    public void payGenericMana(ManaPool pool, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ManaColor highestColor = null;
            int highestAmount = 0;
            for (ManaColor color : ManaColor.values()) {
                int available = pool.get(color);
                if (available > highestAmount) {
                    highestAmount = available;
                    highestColor = color;
                }
            }
            if (highestColor != null) {
                pool.remove(highestColor);
                remaining--;
            } else {
                break;
            }
        }
    }

    // ===== Clone / Legend =====

    public void applyCloneCopy(Permanent clonePerm, Permanent targetPerm, Integer powerOverride, Integer toughnessOverride) {
        Card target = targetPerm.getCard();
        Card copy = new Card();
        copy.setName(target.getName());
        copy.setType(target.getType());
        copy.setManaCost(target.getManaCost());
        copy.setColor(target.getColor());
        copy.setSupertypes(target.getSupertypes());
        copy.setSubtypes(target.getSubtypes());
        copy.setCardText(target.getCardText());
        copy.setPower(powerOverride != null ? powerOverride : target.getPower());
        copy.setToughness(toughnessOverride != null ? toughnessOverride : target.getToughness());
        copy.setKeywords(target.getKeywords());
        copy.setSetCode(target.getSetCode());
        copy.setCollectorNumber(target.getCollectorNumber());
        boolean hasPTOverride = powerOverride != null || toughnessOverride != null;
        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration reg : target.getEffectRegistrations(slot)) {
                // CR 707.9d: when a copy effect provides specific P/T values,
                // characteristic-defining abilities that define P/T are not copied
                if (hasPTOverride && reg.effect().isPowerToughnessDefining()) {
                    continue;
                }
                copy.addEffect(slot, reg.effect(), reg.triggerMode());
            }
        }
        for (ActivatedAbility ability : target.getActivatedAbilities()) {
            copy.addActivatedAbility(ability);
        }
        clonePerm.setCard(copy);
    }

    // ===== Clone replacement effect =====

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId) {
        CopyPermanentOnEnterEffect copyEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof CopyPermanentOnEnterEffect)
                .map(e -> (CopyPermanentOnEnterEffect) e)
                .findFirst().orElse(null);
        if (copyEffect == null) return false;

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (gameQueryService.matchesPermanentPredicate(gameData, p, copyEffect.filter())) {
                validIds.add(p.getId());
            }
        });

        if (validIds.isEmpty()) return false;

        gameData.cloneOperation.card = card;
        gameData.cloneOperation.controllerId = controllerId;
        gameData.cloneOperation.etbTargetId = targetPermanentId;
        gameData.cloneOperation.powerOverride = copyEffect.powerOverride();
        gameData.cloneOperation.toughnessOverride = copyEffect.toughnessOverride();
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CloneCopy());

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                card,
                controllerId,
                List.of(copyEffect),
                card.getName() + " — You may have it enter as a copy of any " + copyEffect.typeLabel() + " on the battlefield."
        ));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    public void completeCloneEntry(GameData gameData, UUID targetPermanentId) {
        Card card = gameData.cloneOperation.card;
        UUID controllerId = gameData.cloneOperation.controllerId;
        UUID etbTargetId = gameData.cloneOperation.etbTargetId;
        Integer powerOverride = gameData.cloneOperation.powerOverride;
        Integer toughnessOverride = gameData.cloneOperation.toughnessOverride;

        gameData.cloneOperation.card = null;
        gameData.cloneOperation.controllerId = null;
        gameData.cloneOperation.etbTargetId = null;
        gameData.cloneOperation.powerOverride = null;
        gameData.cloneOperation.toughnessOverride = null;

        Permanent perm = new Permanent(card);

        if (targetPermanentId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermanentId);
            if (targetPerm != null) {
                applyCloneCopy(perm, targetPerm, powerOverride, toughnessOverride);
            }
        }

        putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        String originalName = card.getName();
        if (targetPermanentId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermanentId);
            String targetName = targetPerm != null ? targetPerm.getCard().getName() : perm.getCard().getName();
            String logEntry = originalName + " enters the battlefield as a copy of " + targetName + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} enters as copy of {} for {}", gameData.id, originalName, targetName, playerName);
        } else {
            String logEntry = originalName + " enters the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} enters battlefield without copying for {}", gameData.id, originalName, playerName);
        }

        handleCreatureEnteredBattlefield(gameData, controllerId, perm.getCard(), etbTargetId, true);

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    // ===== ETB pipeline =====

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId, boolean wasCastFromHand) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetPermanentId, wasCastFromHand, 0);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId, boolean wasCastFromHand, int etbMode) {
        boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseColorEffect);
        if (needsColorChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), targetPermanentId);
            return;
        }

        processCreatureETBEffects(gameData, controllerId, card, targetPermanentId, wasCastFromHand, etbMode);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId, boolean wasCastFromHand) {
        processCreatureETBEffects(gameData, controllerId, card, targetPermanentId, wasCastFromHand, 0);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetPermanentId, boolean wasCastFromHand, int etbMode) {
        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
                .filter(e -> !(e instanceof CopyPermanentOnEnterEffect))
                .filter(e -> !(e instanceof EnterWithXChargeCountersEffect))
                .filter(e -> !(e instanceof EnterWithFixedChargeCountersEffect))
                .toList();
        if (!triggeredEffects.isEmpty()) {
            List<CardEffect> mayEffects = triggeredEffects.stream().filter(e -> e instanceof MayEffect).toList();
            List<CardEffect> mandatoryEffects = triggeredEffects.stream()
                    .filter(e -> !(e instanceof MayEffect))
                    .map(e -> {
                        if (e instanceof LoseGameIfNotCastFromHandEffect) {
                            return wasCastFromHand ? null : new TargetPlayerLosesGameEffect(controllerId);
                        }
                        // Unwrap modal ETB choice (choose one) using the mode index from cast time
                        if (e instanceof ChooseOneEffect coe) {
                            if (etbMode >= 0 && etbMode < coe.options().size()) {
                                return coe.options().get(etbMode).effect();
                            }
                            return coe.options().getFirst().effect();
                        }
                        return e;
                    })
                    .filter(Objects::nonNull)
                    // Metalcraft intervening-if: only trigger if controller has 3+ artifacts
                    .filter(e -> {
                        if (e instanceof MetalcraftConditionalEffect) {
                            return gameQueryService.isMetalcraftMet(gameData, controllerId);
                        }
                        return true;
                    })
                    .toList();

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                gameData.queueMayAbility(card, controllerId, may);
            }

            if (!mandatoryEffects.isEmpty()) {
                // Separate graveyard exile effects (need multi-target selection at trigger time)
                List<CardEffect> graveyardExileEffects = mandatoryEffects.stream()
                        .filter(e -> e instanceof ExileCardsFromGraveyardEffect).toList();
                List<CardEffect> otherEffects = mandatoryEffects.stream()
                        .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect)).toList();

                // Put non-graveyard-exile effects on the stack as before
                if (!otherEffects.isEmpty()) {
                    if (!card.isNeedsTarget() || targetPermanentId != null) {
                        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                        UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s ETB ability",
                                new ArrayList<>(otherEffects),
                                0,
                                targetPermanentId,
                                sourcePermanentId,
                                Map.of(),
                                null,
                                List.of(),
                                List.of()
                        ));
                        String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, etbLog);
                        log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                    }
                }

                // Handle graveyard exile effects: targets must be chosen at trigger time
                for (CardEffect effect : graveyardExileEffects) {
                    ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
                    handleGraveyardExileETBTargeting(gameData, controllerId, card, mandatoryEffects, exile);
                }
            }
        }

        checkAllyCreatureEntersTriggers(gameData, controllerId, card);
        checkAllyArtifactEntersTriggers(gameData, controllerId, card);
        checkAllyNontokenArtifactEntersTriggers(gameData, controllerId, card);
        checkAnyCreatureEntersTriggers(gameData, controllerId, card);
        if (card.getType() == CardType.LAND) {
            checkOpponentLandEntersTriggers(gameData, controllerId);
        }
    }

    private void handleGraveyardExileETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                   List<CardEffect> allEffects, ExileCardsFromGraveyardEffect exile) {
        // Collect all cards from all graveyards
        List<UUID> allCardIds = new ArrayList<>();
        List<CardView> allCardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                allCardIds.add(graveyardCard.getId());
                allCardViews.add(cardViewFactory.create(graveyardCard));
            }
        }

        if (allCardIds.isEmpty()) {
            // No graveyard cards: put ability on stack with 0 targets (just gains life on resolution)
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(allEffects),
                    List.of()
            ));
            String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no graveyard cards)", gameData.id, card.getName());
        } else {
            // Prompt player to choose targets before putting ability on the stack
            int maxTargets = Math.min(exile.maxTargets(), allCardIds.size());
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(allEffects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, allCardIds, allCardViews, maxTargets,
                    "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "") + " from graveyards to exile.");
        }
    }

    void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                       StackEntryType entryType, int xValue) {
        // Collect creature cards from controller's own graveyard
        List<UUID> creatureCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.getType() == CardType.CREATURE) {
                    creatureCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = xValue;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, creatureCardIds, cardViews, xValue,
                "Choose " + xValue + " target creature card" + (xValue != 1 ? "s" : "") + " from your graveyard to exile.");
    }

    void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                StackEntryType entryType, CardPredicate filter) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = matchingCardIds.size();
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose any number of target " + filterLabel + "s from your graveyard.");
    }

    void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                            StackEntryType entryType, CardPredicate filter, int maxTargetsCap) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCardIds.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from your graveyard.");
    }

    void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof GainLifeEqualToToughnessEffect) {
                    int toughness = enteringCreature.getToughness();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            List.of(new GainLifeEffect(toughness))
                    ));
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(controllerId) + " will gain " + toughness + " life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (toughness={})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), toughness);
                }
            }
}
    }

    void checkAllyArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        boolean isArtifact = enteringCard.getType() == CardType.ARTIFACT
                || (enteringCard.getAdditionalTypes() != null && enteringCard.getAdditionalTypes().contains(CardType.ARTIFACT));
        if (!isArtifact) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers for {} entering (ally artifact entered)",
                        gameData.id, perm.getCard().getName(), enteringCard.getName());
            }
        }
    }

    void checkAllyNontokenArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.isToken()) return;

        boolean isArtifact = enteringCard.getType() == CardType.ARTIFACT
                || (enteringCard.getAdditionalTypes() != null && enteringCard.getAdditionalTypes().contains(CardType.ARTIFACT));
        if (!isArtifact) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        // Find the entering permanent's ID
        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayPayManaEffect mayPay) {
                    gameData.queueMayAbility(perm.getCard(), controllerId, mayPay, enteringPermanentId);
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                } else if (effect instanceof MayEffect may) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                }
            }
        }
    }

    void checkOpponentLandEntersTriggers(GameData gameData, UUID landControllerId) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(landControllerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect effectToResolve = effect;

                    if (effect instanceof PermanentEnteredThisTurnConditionalEffect conditional) {
                        List<Card> entered = gameData.permanentsEnteredBattlefieldThisTurn
                                .getOrDefault(landControllerId, List.of());
                        long matchCount = entered.stream()
                                .filter(c -> gameQueryService.matchesCardPredicate(c, conditional.predicate(), null))
                                .count();
                        if (matchCount < conditional.minCount()) continue;
                        effectToResolve = conditional.wrapped();
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effectToResolve)),
                            landControllerId,
                            perm.getId()
                    ));

                    String logEntry = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} triggers on opponent land entering", gameData.id, perm.getCard().getName());
                }
            }
        });
    }

    public void checkAllyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayPayManaEffect mayPay) {
                    gameData.queueMayAbility(perm.getCard(), dyingCreatureControllerId, mayPay, null);
                } else if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), dyingCreatureControllerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            dyingCreatureControllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                }
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkEquippedCreatureDeathTriggers(GameData gameData, UUID dyingCreatureId, UUID dyingCreatureControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (!dyingCreatureId.equals(perm.getAttachedTo())) continue;
            if (!perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingCreatureControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers (equipped creature died).";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (equipped creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID artifactControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD);
            if (effects != null && !effects.isEmpty()) {
                for (CardEffect effect : effects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else {
                        // For effects that damage the artifact's controller, pre-set the target
                        UUID targetId = (effect instanceof DealDamageToTriggeringPermanentControllerEffect) ? artifactControllerId : null;
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                targetId,
                                perm.getId()
                        ));
                    }
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", gameData.id, perm.getCard().getName());
                }
            }

            // Opponent-specific triggers: only fire if the graveyard owner is an opponent of this permanent's controller
            if (!playerId.equals(graveyardOwnerId)) {
                List<CardEffect> opponentEffects = perm.getCard().getEffects(EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD);
                if (opponentEffects != null && !opponentEffects.isEmpty()) {
                    for (CardEffect effect : opponentEffects) {
                        if (effect instanceof MayEffect may) {
                            gameData.queueMayAbility(perm.getCard(), playerId, may);
                        } else {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(),
                                    playerId,
                                    perm.getCard().getName() + "'s ability",
                                    new ArrayList<>(List.of(effect)),
                                    null,
                                    perm.getId()
                            ));
                        }
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", gameData.id, perm.getCard().getName());
                    }
                }
            }
        });
    }

    public void checkAnyNontokenCreatureDeathTriggers(GameData gameData, Card dyingCard) {
        if (dyingCard.isToken()) return;

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may && may.wrapped() instanceof ImprintDyingCreatureEffect) {
                    ImprintDyingCreatureEffect imprintEffect = new ImprintDyingCreatureEffect(dyingCard.getId());
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            playerId,
                            List.of(imprintEffect),
                            perm.getCard().getName() + " — " + may.prompt()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s imprint ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} imprint triggers (nontoken creature died)", gameData.id, perm.getCard().getName());
                } else if (effect instanceof MayPayManaEffect mayPay
                        && mayPay.wrapped() instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect) {
                    // Nim Deathmantle pattern: only trigger for creatures in this player's graveyard
                    List<Card> playerGraveyard = gameData.playerGraveyards.get(playerId);
                    if (playerGraveyard == null || playerGraveyard.stream().noneMatch(c -> c.getId().equals(dyingCard.getId()))) {
                        return;
                    }
                    var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(dyingCard.getId());
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            perm.getCard(),
                            playerId,
                            List.of(returnEffect),
                            perm.getCard().getName() + " — Pay " + mayPay.manaCost() + " to return " + dyingCard.getName() + " to the battlefield?",
                            dyingCard.getId(),
                            mayPay.manaCost()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers (" + dyingCard.getName() + " died).";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} return trigger fires (nontoken creature {} died)", gameData.id, perm.getCard().getName(), dyingCard.getName());
                }
            }
        });
    }

    public void checkOpponentCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the dying creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(dyingCreatureControllerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), playerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            dyingCreatureControllerId,
                            perm.getId()
                    ));
                }
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (opponent creature died)", gameData.id, perm.getCard().getName());
            }
        });
    }

    void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        // Non-creature permanents (e.g. artifacts) should not trigger "creature enters" triggers
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            if (perm.getCard() == enteringCreature) return;

            for (CardEffect effect : effects) {
                if (effect instanceof GainLifeEffect gainLife) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            List.of(new GainLifeEffect(gainLife.amount()))
                    ));
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(playerId) + " will gain " + gainLife.amount() + " life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (gain {} life)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), gainLife.amount());
                }
            }
        });
    }

    public void checkOpponentDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(drawingPlayerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DRAWS);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect effect : drawEffects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                drawingPlayerId,
                                perm.getId()
                        ));
                    }

                    String logEntry = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} triggers on opponent draw", gameData.id, perm.getCard().getName());
                }
            }
        });
    }

    // ===== Draw =====

    public void resolveDrawCard(GameData gameData, UUID playerId) {
        Card abundanceSource = findAbundanceSourceCard(gameData, playerId);
        if (abundanceSource != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    abundanceSource,
                    playerId,
                    List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.ABUNDANCE)),
                    "Replace this draw with Abundance?"
            ));
            return;
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);
            performDrawCard(gameData, replacementController);
            return;
        }

        performDrawCard(gameData, playerId);
    }

    public void resolveDrawCardWithoutStaticReplacementCheck(GameData gameData, UUID playerId) {
        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);

            if (replacementController.equals(playerId)) {
                performDrawCard(gameData, replacementController);
            } else {
                resolveDrawCard(gameData, replacementController);
            }
            return;
        }

        performDrawCard(gameData, playerId);
    }

    private Card findAbundanceSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasAbundanceEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof AbundanceDrawReplacementEffect);
            if (hasAbundanceEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    void performDrawCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameData.playersAttemptedDrawFromEmptyLibrary.add(playerId);
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to draw.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            // CR 704.5b — player who attempted to draw from an empty library loses the game
            if (gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String lossLog = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                gameBroadcastService.logAndBroadcast(gameData, lossLog);
                log.info("Game {} - {} loses (drew from empty library)", gameData.id, gameData.playerIdToName.get(playerId));
                declareWinner(gameData, winnerId);
            }
            return;
        }

        Card drawn = deck.removeFirst();
        hand.add(drawn);

        // Track cards drawn this turn (for Molten Psyche, etc.)
        gameData.cardsDrawnThisTurn.merge(playerId, 1, Integer::sum);

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));

        checkControllerDrawTriggers(gameData, playerId);
        checkOpponentDrawTriggers(gameData, playerId);
    }

    public void checkControllerDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(drawingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DRAWS);
            if (drawEffects == null || drawEffects.isEmpty()) continue;

            for (CardEffect effect : drawEffects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), drawingPlayerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            drawingPlayerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            drawingPlayerId,
                            perm.getId()
                    ));

                    String triggerLog = perm.getCard().getName() + " triggers — each opponent loses life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} controller-draw trigger pushed onto stack",
                            gameData.id, perm.getCard().getName());
                }
            }
        }
    }

    // ===== Regeneration =====

    public boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.isCantRegenerateThisTurn()) {
            return false;
        }
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            perm.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
            perm.setAttacking(false);
            perm.setBlocking(false);
            perm.getBlockingTargets().clear();

            String logEntry = perm.getCard().getName() + " regenerates.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Exile (used by CombatService) =====

    public void resolveExileTopCardsRepeatOnDuplicate(GameData gameData, Card sourceCard, UUID targetPlayerId, ExileTopCardsRepeatOnDuplicateEffect effect) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> exiled = gameData.playerExiledCards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String creatureName = sourceCard.getName();

        String triggerLog = creatureName + "'s ability triggers — " + playerName + " exiles cards from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);

        boolean repeat = true;
        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty. No cards to exile.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                break;
            }

            int cardsToExile = Math.min(effect.count(), deck.size());
            List<Card> exiledThisRound = new ArrayList<>();
            for (int i = 0; i < cardsToExile; i++) {
                Card card = deck.removeFirst();
                exiled.add(card);
                exiledThisRound.add(card);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(playerName).append(" exiles ");
            for (int i = 0; i < exiledThisRound.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(exiledThisRound.get(i).getName());
            }
            sb.append(".");
            gameBroadcastService.logAndBroadcast(gameData, sb.toString());

            Set<String> seen = new HashSet<>();
            for (Card card : exiledThisRound) {
                if (!seen.add(card.getName())) {
                    repeat = true;
                    break;
                }
            }

            if (repeat) {
                String repeatLog = "Two or more exiled cards share the same name — repeating the process.";
                gameBroadcastService.logAndBroadcast(gameData, repeatLog);
            }
        }

        log.info("Game {} - {} exile trigger resolved for {}", gameData.id, creatureName, playerName);
    }
}




