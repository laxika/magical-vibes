package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingBoostSourceByDiscardedManaValue;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType;
import com.github.laxika.magicalvibes.model.PendingTransformOnCreatureDiscard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardChoiceHandlerService {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final EquipSupport equipSupport;
    private final ExileService exileService;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService permanentRemovalService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    /** Answers CARD_CHOICE and TARGETED_CARD_CHOICE (put a card/Aura from hand onto the battlefield). */
    public void handleHandCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        UUID choicePlayerId;
        List<Integer> validIndices;
        UUID targetId;
        boolean isTargeted;
        boolean enterTapped = false;
        boolean grantHaste = false;
        boolean sacrificeAtEndStep = false;
        boolean enterAttacking = false;
        boolean drawAndRepeat = false;
        boolean putAnyNumber = false;
        CardPredicate drawAndRepeatPredicate = null;
        String drawAndRepeatLabel = null;
        UUID attachEquipmentCardId = null;
        UUID exileSourceIfDeclinedId = null;
        Integer sacrificeUnlessPayGenericReduction = null;
        if (active instanceof PendingInteraction.HandCardChoice hc) {
            choicePlayerId = hc.playerId();
            validIndices = hc.validIndices();
            targetId = null;
            isTargeted = false;
            enterTapped = hc.enterTapped();
            grantHaste = hc.grantHaste();
            sacrificeAtEndStep = hc.sacrificeAtEndStep();
            attachEquipmentCardId = hc.attachEquipmentCardId();
            enterAttacking = hc.enterAttacking();
            sacrificeUnlessPayGenericReduction = hc.sacrificeUnlessPayGenericReduction();
            drawAndRepeat = hc.drawAndRepeat();
            putAnyNumber = hc.putAnyNumber();
            drawAndRepeatPredicate = hc.drawAndRepeatPredicate();
            drawAndRepeatLabel = hc.drawAndRepeatLabel();
        } else if (active instanceof PendingInteraction.TargetedHandCardChoice thc) {
            choicePlayerId = thc.playerId();
            validIndices = thc.validIndices();
            targetId = thc.targetId();
            isTargeted = true;
            exileSourceIfDeclinedId = thc.exileSourceIfDeclinedId();
        } else {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (!player.getId().equals(choicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();

        gameData.interaction.clearAwaitingInput();

        if (cardIndex == -1) {
            String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());

            // "If you don't, exile this creature." (Evershrike) — declining exiles the source permanent.
            if (exileSourceIfDeclinedId != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, exileSourceIfDeclinedId);
                if (source != null) {
                    permanentRemovalService.removePermanentToExile(gameData, source);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.isExiled(source.getCard()));
                    log.info("Game {} - {} is exiled (no Aura put onto it)", gameData.id, source.getCard().getName());
                }
            }
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.remove(cardIndex);

            if (isTargeted) {
                resolveTargetedCardChoice(gameData, player, playerId, hand, card, targetId);
            } else {
                resolveUntargetedCardChoice(gameData, player, playerId, hand, card, enterTapped, grantHaste,
                        sacrificeAtEndStep, attachEquipmentCardId, enterAttacking, sacrificeUnlessPayGenericReduction);
                // Cultivator Colossus / Wrenn and Seven: re-offer until decline / no matches.
                if ((drawAndRepeat || putAnyNumber) && drawAndRepeatPredicate != null && drawAndRepeatLabel != null
                        && !gameData.interaction.isAwaitingInput()) {
                    if (drawAndRepeat) {
                        drawService.resolveDrawCard(gameData, playerId);
                    }
                    playerInteractionSupport.applyPutCardToBattlefield(gameData, playerId,
                            new PutCardToBattlefieldEffect(drawAndRepeatPredicate, drawAndRepeatLabel, enterTapped,
                                    false, false, false, false, false, drawAndRepeat, putAnyNumber));
                }
            }
        }

        // A pay-or-sacrifice may ability may now be awaiting the player's decision (e.g. Flash).
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    /** Answers DISCARD_CHOICE, including the multi-pick countdown carried on the record. */
    public void handleDiscardCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.DiscardChoice discardChoice =
                gameData.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        if (discardChoice == null || !player.getId().equals(discardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Integer> validIndices = discardChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            // Invalid index (e.g. player clicked "Decline" sending -1) — re-prompt the discard choice
            // Preserve filtered validIndices/prompt (e.g. DiscardCardThenEffect land-only discard).
            log.warn("Game {} - {} sent invalid discard card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginDiscardChoice(gameData, player.getId(), discardChoice.validIndices(),
                    discardChoice.prompt(), discardChoice.remainingCount(), discardChoice.followUp());
            return;
        }

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        boolean replacedByBattlefield = false;
        if (hasEnterBattlefieldOnDiscardEffect(card) && gameData.discardCausedByOpponent) {
            // Replacement effect: put onto battlefield instead of graveyard (e.g. Obstinate Baloth)
            Permanent permanent = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    player.getUsername() + " discards ", card, " — it enters the battlefield instead."));
            log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield", gameData.id, player.getUsername(), card.getName());
            replacedByBattlefield = true;
        } else {
            graveyardService.discardCard(gameData, playerId, card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.playerDiscards(player.getUsername(), card));
            log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());
        }

        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);

        if (replacedByBattlefield && card.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
        }

        // Check if a spell should return to hand based on the discarded card type (e.g. Psychic Miasma)
        checkPendingReturnToHandOnDiscard(gameData, card);

        // Check if a creature discard should untap + transform the source (e.g. Civilized Scholar)
        checkPendingTransformOnCreatureDiscard(gameData, card);

        // Check if the discarded card should pump the source by its mana value (e.g. Spellbound Dragon)
        checkPendingBoostSourceByDiscardedManaValue(gameData, card);

        int remainingDiscards = Math.max(discardChoice.remainingCount() - 1, 0);

        if (remainingDiscards > 0 && !hand.isEmpty()) {
            gameBroadcastService.broadcastGameState(gameData);
            playerInputService.beginDiscardChoice(gameData, playerId, remainingDiscards, discardChoice.followUp());
        } else {
            DiscardFollowUp followUp = discardChoice.followUp();
            gameData.interaction.clearAwaitingInput();
            finalizePendingReturnToHandOnDiscard(gameData);

            // After cleanup discard, apply end-of-turn resets (CR 514.2)
            if (gameData.cleanupDiscardPending) {
                gameData.cleanupDiscardPending = false;
                turnProgressionService.applyCleanupResets(gameData);
            }

            // Continue "each player discards" queue (e.g. Serum Raker's death trigger)
            if (!followUp.remainingEachPlayerDiscards().isEmpty()) {
                playerInteractionSupport.startNextEachPlayerDiscard(gameData, followUp);
                return;
            }

            // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
            if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
                triggerCollectionService.processNextDiscardSelfTrigger(gameData);
                return;
            }

            // Draw cards after "discard up to N, then draw that many" completes
            if (followUp.rummageDrawCount() > 0) {
                int drawCount = followUp.rummageDrawCount();
                for (int i = 0; i < drawCount; i++) {
                    drawService.resolveDrawCard(gameData, playerId);
                }
                String drawPlayerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(drawPlayerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + "."));
            }

            // Untap permanent after "discard a card, then untap [source]" completes
            if (followUp.untapPermanentId() != null) {
                UUID permanentId = followUp.untapPermanentId();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf == null) continue;
                    for (Permanent p : bf) {
                        if (p.getId().equals(permanentId)) {
                            p.untap();
                            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(p.getCard(), " untaps."));
                            break;
                        }
                    }
                }
            }

            // Boost permanent after "discard a card, then this creature gets +X/+Y" completes
            if (followUp.boostPermanentId() != null) {
                UUID permanentId = followUp.boostPermanentId();
                int powerBoost = followUp.boostPower();
                int toughnessBoost = followUp.boostToughness();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> bf = gameData.playerBattlefields.get(pid);
                    if (bf == null) continue;
                    for (Permanent p : bf) {
                        if (p.getId().equals(permanentId)) {
                            p.setPowerModifier(p.getPowerModifier() + powerBoost);
                            p.setToughnessModifier(p.getToughnessModifier() + toughnessBoost);
                            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                                    .card(p.getCard())
                                    .text(String.format(" gets %+d/%+d until end of turn.",
                                            powerBoost, toughnessBoost))
                                    .build());
                            break;
                        }
                    }
                }
            }

            // Return cards from graveyard to hand after "discard X cards, then return a card for
            // each discarded" completes (Recall). One sequential pick per discarded card; the
            // graveyard holds at least that many (the cards just discarded), so the choice always
            // begins. Once the queue empties, GraveyardChoiceHandlerService resumes the remaining
            // effects (e.g. the trailing ExileSpellEffect).
            if (followUp.graveyardReturnCount() > 0) {
                for (int i = 0; i < followUp.graveyardReturnCount(); i++) {
                    gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                            playerId, 1, null, GraveyardChoiceDestination.HAND, false));
                }
                graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            // Push "if you do" rider after a filtered discard (DiscardCardThenEffect / Pack Guardian)
            if (followUp.thenEffect() != null && followUp.thenEffectSourceCard() != null) {
                CardEffect thenEffect = followUp.thenEffect();
                Card sourceCard = followUp.thenEffectSourceCard();
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        playerId,
                        sourceCard.getName() + "'s effect",
                        List.of(thenEffect)
                ));
                log.info("Game {} - {} discard-then rider pushed for {}",
                        gameData.id, player.getUsername(), sourceCard.getName());
            }

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. "Target player discards a card, then mills a card.")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            // A resumed effect may have created a pending may ability
            // (e.g. Frightful Delusion: discard → counter-unless-pay)
            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    /** Answers EXILE_FROM_HAND_CHOICE, including the multi-pick countdown carried on the record. */
    public void handleExileFromHandChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.ExileFromHandChoice exileChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ExileFromHandChoice.class);
        if (exileChoice == null || !player.getId().equals(exileChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Integer> validIndices = exileChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            log.warn("Game {} - {} sent invalid exile card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginExileFromHandChoice(gameData, player.getId(), exileChoice.sourcePermanentId(),
                    exileChoice.playPermissionControllerId(), exileChoice.remainingCount(),
                    exileChoice.remainingChoosers(), exileChoice.cardsPerPlayer());
            return;
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = exileChoice.sourcePermanentId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        // Add to player's exile zone, tracked with source permanent if available (e.g. Karn Liberated)
        if (sourcePermanentId != null) {
            exileService.exileCard(gameData, playerId, card, sourcePermanentId);
        } else {
            exileService.exileCard(gameData, playerId, card);
        }

        // Grant the controlling player permission to play this card for as long as it remains
        // exiled (e.g. Fiend of the Shadows). Does not expire at end of turn.
        if (exileChoice.playPermissionControllerId() != null) {
            gameData.exilePlayPermissions.put(card.getId(), exileChoice.playPermissionControllerId());
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", card, " from hand."));
        log.info("Game {} - {} exiles {} from hand", gameData.id, player.getUsername(), card.getName());

        int remainingExiles = Math.max(exileChoice.remainingCount() - 1, 0);

        if (remainingExiles > 0 && !hand.isEmpty()) {
            gameBroadcastService.broadcastGameState(gameData);
            playerInputService.beginExileFromHandChoice(gameData, playerId, sourcePermanentId,
                    exileChoice.playPermissionControllerId(), remainingExiles,
                    exileChoice.remainingChoosers(), exileChoice.cardsPerPlayer());
        } else if (exileChoice.remainingChoosers() != null && !exileChoice.remainingChoosers().isEmpty()) {
            // Next opponent in the each-opponent exile queue (Nicol Bolas, God-Pharaoh +1).
            UUID next = exileChoice.remainingChoosers().getFirst();
            List<UUID> rest = exileChoice.remainingChoosers().size() > 1
                    ? List.copyOf(exileChoice.remainingChoosers().subList(1, exileChoice.remainingChoosers().size()))
                    : List.of();
            gameBroadcastService.broadcastGameState(gameData);
            playerInputService.beginExileFromHandChoice(gameData, next, sourcePermanentId,
                    exileChoice.playPermissionControllerId(), exileChoice.cardsPerPlayer(), rest,
                    exileChoice.cardsPerPlayer());
        } else {
            gameData.interaction.clearAwaitingInput();

            // Resume resolving remaining effects
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void handleRevealedHandCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.RevealedHandChoice revealedHandChoice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        if (revealedHandChoice == null || !player.getId().equals(revealedHandChoice.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Optional choice declined (e.g. Vendilion Clique: "you may choose a nonland card").
        if (cardIndex == -1 && revealedHandChoice.optional()) {
            handleRevealedHandChoiceDeclined(gameData, player, revealedHandChoice);
            return;
        }

        List<Integer> validIndices = revealedHandChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = revealedHandChoice.targetPlayerId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Card chosenCard = targetHand.remove(cardIndex);
        List<Card> chosenCards = new ArrayList<>(revealedHandChoice.chosenCards());
        chosenCards.add(chosenCard);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                player.getUsername() + " chooses ", chosenCard, " from " + targetName + "'s hand."));
        log.info("Game {} - {} chooses {} from {}'s hand", gameData.id, player.getUsername(), chosenCard.getName(), targetName);

        int remainingChoices = Math.max(revealedHandChoice.remainingCount() - 1, 0);
        boolean discardMode = revealedHandChoice.discardMode();
        boolean exileMode = revealedHandChoice.exileMode();
        boolean bottomThenDrawMode = revealedHandChoice.bottomThenDrawMode();

        // Distended Mindbender: after the first filtered pick, begin a second pick under followUpFilter.
        if (remainingChoices == 0 && revealedHandChoice.followUpFilter() != null && !targetHand.isEmpty()) {
            CardPredicate followUp = revealedHandChoice.followUpFilter();
            List<Integer> followUpIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(targetHand.get(i), followUp, null)) {
                    followUpIndices.add(i);
                }
            }
            if (!followUpIndices.isEmpty()) {
                String followUpPrompt = revealedHandChoice.followUpPrompt() != null
                        ? revealedHandChoice.followUpPrompt()
                        : "Choose another card to discard.";
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                        player.getId(), targetPlayerId, followUpIndices, 1,
                        discardMode, exileMode, chosenCards, revealedHandChoice.sourcePermanentId(),
                        followUpPrompt, false, false,
                        revealedHandChoice.gainLifeToChooserEqualToChosenToughness(), null, null));
                return;
            }
            // No second-band match — fall through and discard only what was already chosen.
        }

        if (remainingChoices > 0 && !targetHand.isEmpty()) {
            // More cards to choose — update valid indices and prompt again
            List<Integer> newValidIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                newValidIndices.add(i);
            }

            String prompt;
            if (discardMode) {
                prompt = "Choose another card to discard.";
            } else if (exileMode) {
                prompt = "Choose another card to exile.";
            } else {
                prompt = "Choose another card to put on top of " + targetName + "'s library.";
            }
            // Matching the legacy mid-flow re-begin, sourcePermanentId is not carried across picks.
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                    player.getId(), targetPlayerId, newValidIndices, remainingChoices,
                    discardMode, exileMode, chosenCards, null, prompt, false, false));
        } else {
            // All cards chosen
            gameData.interaction.clearAwaitingInput();

            if (discardMode) {
                // Talara's Bane: the chooser gains life equal to the chosen card's toughness before discard.
                if (revealedHandChoice.gainLifeToChooserEqualToChosenToughness()) {
                    int toughness = chosenCards.stream()
                            .mapToInt(c -> c.getToughness() != null ? c.getToughness() : 0)
                            .sum();
                    lifeSupport.applyGainLife(gameData, player.getId(), toughness);
                }

                // Discard chosen cards to graveyard (or battlefield if replacement effect applies)
                List<Card> replacedCards = new ArrayList<>();
                for (Card discarded : chosenCards) {
                    if (hasEnterBattlefieldOnDiscardEffect(discarded) && gameData.discardCausedByOpponent) {
                        Permanent permanent = new Permanent(discarded);
                        battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);
                        replacedCards.add(discarded);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                                targetName + " discards ", discarded, " — it enters the battlefield instead."));
                        log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield",
                                gameData.id, targetName, discarded.getName());
                    } else {
                        graveyardService.discardCard(gameData, targetPlayerId, discarded);
                    }
                }

                List<Card> normallyDiscarded = chosenCards.stream()
                        .filter(c -> !replacedCards.contains(c))
                        .toList();
                if (!normallyDiscarded.isEmpty()) {
                    String cardNames = String.join(", ", normallyDiscarded.stream().map(Card::getName).toList());
                    gameBroadcastService.logAndBroadcast(gameData,
                            appendCards(GameLog.builder().text(targetName + " discards "), normallyDiscarded)
                                    .text(".").build());
                    log.info("Game {} - {} discards {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);
                }

                for (Card discarded : chosenCards) {
                    triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, discarded);
                }

                for (Card replaced : replacedCards) {
                    if (replaced.hasType(CardType.CREATURE)) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetPlayerId, replaced, null, false);
                    }
                }
            } else if (exileMode) {
                // Exile chosen cards
                for (Card exiled : chosenCards) {
                    exileService.exileCard(gameData, targetPlayerId, exiled);
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                gameBroadcastService.logAndBroadcast(gameData,
                        appendCards(GameLog.builder().text(player.getUsername() + " exiles "), chosenCards)
                                .text(" from " + targetName + "'s hand.").build());
                log.info("Game {} - {} exiles {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);

                // Track return-on-source-leave for exile-until-leaves effects (e.g. Kitesail Freebooter)
                UUID sourcePermanentId = revealedHandChoice.sourcePermanentId();
                if (sourcePermanentId != null) {
                    for (Card exiled : chosenCards) {
                        gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                                new PendingExileReturn(exiled, targetPlayerId, false, true));
                    }
                }
            } else if (bottomThenDrawMode) {
                // Vendilion Clique: reveal chosen card, put it on the bottom of the library, then draw a card.
                List<Card> deck = gameData.playerDecks.get(targetPlayerId);
                for (Card chosen : chosenCards) {
                    deck.addLast(chosen);
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                gameBroadcastService.logAndBroadcast(gameData,
                        appendCards(GameLog.builder().text(targetName + " reveals "), chosenCards)
                                .text(", puts it on the bottom of their library, then draws a card.").build());
                log.info("Game {} - {} bottoms {} from {}'s hand and {} draws", gameData.id,
                        player.getUsername(), cardNames, targetName, targetName);

                drawService.resolveDrawCard(gameData, targetPlayerId);
            } else {
                // Put chosen cards on top of library
                List<Card> deck = gameData.playerDecks.get(targetPlayerId);

                // Insert in reverse order so first chosen ends up on top
                for (int i = chosenCards.size() - 1; i >= 0; i--) {
                    deck.addFirst(chosenCards.get(i));
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                gameBroadcastService.logAndBroadcast(gameData,
                        appendCards(GameLog.builder().text(player.getUsername() + " puts "), chosenCards)
                                .text(" on top of " + targetName + "'s library.").build());
                log.info("Game {} - {} puts {} on top of {}'s library", gameData.id, player.getUsername(), cardNames, targetName);
            }

            // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
            if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
                triggerCollectionService.processNextDiscardSelfTrigger(gameData);
                return;
            }

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. Thoughtseize: choose + discard a nonland card, then "you lose 2 life")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    /** The caster declines an optional revealed-hand choice (e.g. Vendilion Clique's "may"). */
    private void handleRevealedHandChoiceDeclined(GameData gameData, Player player,
                                                  PendingInteraction.RevealedHandChoice revealedHandChoice) {
        gameData.interaction.clearAwaitingInput();

        String targetName = gameData.playerIdToName.get(revealedHandChoice.targetPlayerId());
        String declineLog = player.getUsername() + " chooses no card from " + targetName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(declineLog));
        log.info("Game {} - {} declines the revealed-hand choice", gameData.id, player.getUsername());

        // Resume resolving remaining effects on the same spell/ability.
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    /**
     * Answers the Blackmail / Noggin Whack / Thieving Sprite flow
     * ({@link PendingInteraction.RevealCardsDiscardChoice}). In the
     * reveal stage the target player picks a card to reveal (transitioning to the controller's
     * discard choice once the last one is revealed); in the discard stage the controller picks one
     * revealed card and the target discards it.
     */
    public void handleRevealCardsDiscardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.RevealCardsDiscardChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        if (choice == null || !player.getId().equals(choice.decidingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (!choice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = choice.targetPlayerId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (choice.revealStage()) {
            // The target player picks a card (by hand index) to reveal.
            Card chosen = targetHand.get(cardIndex);
            List<UUID> revealed = new ArrayList<>(choice.revealedCardIds());
            revealed.add(chosen.getId());
            int remaining = Math.max(choice.remainingCount() - 1, 0);

            if (remaining > 0) {
                List<Integer> newValid = new ArrayList<>(choice.validIndices());
                newValid.remove(Integer.valueOf(cardIndex));
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealCardsDiscardChoice(
                        choice.decidingPlayerId(), targetPlayerId, choice.controllerId(), true,
                        newValid, remaining, revealed, "Choose another card to reveal.", choice.discardCount()));
            } else {
                gameData.interaction.clearAwaitingInput();
                playerInteractionSupport.beginRevealCardsDiscardStage(gameData, targetPlayerId,
                        choice.controllerId(), revealed, choice.discardCount());
            }
            return;
        }

        // Discard stage: cardIndex is into the revealed set; map it back to the hand.
        UUID chosenId = choice.revealedCardIds().get(cardIndex);
        int handIndex = -1;
        for (int i = 0; i < targetHand.size(); i++) {
            if (targetHand.get(i).getId().equals(chosenId)) {
                handIndex = i;
                break;
            }
        }
        gameData.interaction.clearAwaitingInput();
        if (handIndex >= 0) {
            Card card = targetHand.remove(handIndex);
            String controllerName = player.getUsername();

            if (hasEnterBattlefieldOnDiscardEffect(card) && gameData.discardCausedByOpponent) {
                Permanent permanent = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, targetPlayerId, permanent);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                        targetName + " discards ", card, " — it enters the battlefield instead."));
                log.info("Game {} - {} discards {} — replacement effect puts it onto the battlefield",
                        gameData.id, targetName, card.getName());
                triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
                if (card.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, targetPlayerId, card, null, false);
                }
            } else {
                graveyardService.discardCard(gameData, targetPlayerId, card);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                        controllerName + " chooses ", card, "; " + targetName + " discards it."));
                log.info("Game {} - {} discards {} (chosen by {})", gameData.id, targetName, card.getName(), controllerName);
                triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
            }
        }

        // More cards left to discard (e.g. Noggin Whack chooses two)? Prompt for the next one over
        // the remaining revealed cards before resolving any triggers or the rest of the spell.
        int remainingDiscards = choice.remainingCount() - 1;
        List<UUID> remainingRevealed = new ArrayList<>(choice.revealedCardIds());
        remainingRevealed.remove(chosenId);
        if (remainingDiscards > 0 && !remainingRevealed.isEmpty()) {
            playerInteractionSupport.beginRevealCardsDiscardStageContinuation(gameData, targetPlayerId,
                    choice.controllerId(), remainingRevealed, remainingDiscards, choice.discardCount());
            return;
        }

        // Process any pending self-discard triggers (e.g. Guerrilla Tactics)
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            return;
        }

        // Resume any remaining effects on the same spell/ability.
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    /** Appends {@code cards} as comma-separated card segments (each hoverable) to {@code builder}. */
    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }

    /** Answers IMPRINT_FROM_HAND_CHOICE (exile the chosen card and imprint it on the source permanent). */
    public void handleImprintFromHandCardChosen(GameData gameData, Player player, int cardIndex) {
        PendingInteraction.ImprintFromHandChoice imprintChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        if (imprintChoice == null || !player.getId().equals(imprintChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Integer> validIndices = imprintChoice.validIndices();
        if (!validIndices.contains(cardIndex)) {
            log.warn("Game {} - {} sent invalid imprint card index {}, re-prompting", gameData.id, player.getUsername(), cardIndex);
            playerInputService.beginImprintFromHandChoice(gameData, player.getId(),
                    new ArrayList<>(validIndices), "Choose a card from your hand.", imprintChoice.sourcePermanentId());
            return;
        }

        UUID playerId = player.getId();
        UUID sourcePermanentId = imprintChoice.sourcePermanentId();

        gameData.interaction.clearAwaitingInput();

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        // Add to controller's exile zone
        exileService.exileCard(gameData, playerId, card);

        // Imprint on source permanent
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent != null) {
            gameData.setImprintedCard(sourcePermanent.getCard(), card);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(
                    card, " is exiled and imprinted on ", sourcePermanent.getCard(), "."));
            log.info("Game {} - {} imprinted {} from hand on {}", gameData.id, player.getUsername(), card.getName(), sourcePermanent.getCard().getName());
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(card, " is exiled (source permanent no longer on the battlefield)."));
            log.info("Game {} - Source permanent left battlefield, {} exiled without imprinting", gameData.id, card.getName());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, auraPerm);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .text(player.getUsername() + " puts ")
                    .card(card)
                    .text(" onto the battlefield attached to ")
                    .card(target.getCard())
                    .text(".")
                    .build());
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            gameData.addCardToHand(playerId, card);
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(card, " can't be attached (target left the battlefield)."));
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card,
                                             boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                             UUID attachEquipmentCardId, boolean enterAttacking,
                                             Integer sacrificeUnlessPayGenericReduction) {
        Permanent permanent = new Permanent(card);
        if (enterTapped) {
            permanent.tap();
        }
        if (grantHaste) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent);
        if (enterAttacking) {
            permanent.setAttacking(true);
        }

        String stateSuffix = enterTapped && enterAttacking ? " tapped and attacking"
                : enterTapped ? " tapped"
                : enterAttacking ? " attacking"
                : "";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                player.getUsername() + " puts ", card, " onto the battlefield" + stateSuffix + "."));
        log.info("Game {} - {} puts {} onto the battlefield{}", gameData.id, player.getUsername(), card.getName(),
                stateSuffix);

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);

        // Deathrender: "…and attach this Equipment to it" — attach the source Equipment to the entered creature.
        if (attachEquipmentCardId != null) {
            attachSourceEquipmentToPermanent(gameData, attachEquipmentCardId, permanent);
        }

        if (sacrificeAtEndStep) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }

        // Flash: "sacrifice it unless you pay its mana cost reduced by {N}." Prompt a pay-or-sacrifice
        // may ability against the just-entered creature — accepting charges the reduced cost, declining
        // (or being unable to pay) sacrifices it via the ForcedCostOrElse SacrificeSelf penalty.
        if (sacrificeUnlessPayGenericReduction != null) {
            String reducedCost = reduceGenericCost(card.getManaCost(), sacrificeUnlessPayGenericReduction);
            ForcedCostOrElseEffect payOrSacrifice = new ForcedCostOrElseEffect(
                    new PayManaCost(reducedCost), List.of(new SacrificeSelfEffect()), true);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card, playerId, List.of(payOrSacrifice),
                    card.getName() + " - Pay " + reducedCost + " or sacrifice it?",
                    null, reducedCost, permanent.getId()));
            playerInputService.processNextMayAbility(gameData);
        }
    }

    /**
     * Returns a mana cost string with its generic portion reduced by {@code reduction} (floored at 0);
     * colored, hybrid, and other symbols are preserved. Yields {@code "{0}"} when nothing else remains.
     */
    private static String reduceGenericCost(String manaCost, int reduction) {
        String cost = manaCost == null ? "" : manaCost;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([^}]+)}").matcher(cost);
        int generic = 0;
        StringBuilder others = new StringBuilder();
        while (matcher.find()) {
            String symbol = matcher.group(1);
            try {
                generic += Integer.parseInt(symbol);
            } catch (NumberFormatException notGeneric) {
                others.append('{').append(symbol).append('}');
            }
        }
        int newGeneric = Math.max(0, generic - reduction);
        StringBuilder result = new StringBuilder();
        if (newGeneric > 0 || others.length() == 0) {
            result.append('{').append(newGeneric).append('}');
        }
        result.append(others);
        return result.toString();
    }

    private void attachSourceEquipmentToPermanent(GameData gameData, UUID equipmentCardId, Permanent target) {
        Permanent equipment = equipSupport.findEquipmentByCardId(gameData, equipmentCardId);
        if (equipment == null) {
            return;
        }
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(target.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(
                equipment.getCard(), " is now attached to ", target.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id, equipment.getCard().getName(), target.getCard().getName());
    }

    private void checkPendingReturnToHandOnDiscard(GameData gameData, Card discardedCard) {
        PendingReturnToHandOnDiscardType pending = gameData.pendingReturnToHandOnDiscardType;
        if (pending == null) {
            return;
        }
        if (discardedCard.hasType(pending.requiredType())) {
            gameData.addCardToHand(pending.controllerId(), pending.card());
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(pending.card(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to hand (land discarded)", gameData.id, pending.card().getName());
            gameData.pendingReturnToHandOnDiscardType = null;
        }
    }

    private void checkPendingTransformOnCreatureDiscard(GameData gameData, Card discardedCard) {
        PendingTransformOnCreatureDiscard pending = gameData.pendingTransformOnCreatureDiscard;
        if (pending == null) {
            return;
        }
        gameData.pendingTransformOnCreatureDiscard = null;
        if (discardedCard.hasType(CardType.CREATURE)) {
            Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
            if (source == null) {
                return;
            }
            // Untap
            source.untap();
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(), " untaps."));
            log.info("Game {} - {} untaps (creature discarded)", gameData.id, source.getCard().getName());

            // Transform
            Card originalCard = source.getOriginalCard();
            Card backFace = originalCard.getBackFaceCard();
            if (backFace != null && !source.isTransformed()) {
                Card frontCard = source.getCard();
                source.setCard(backFace);
                source.setTransformed(true);
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardTextCard(frontCard, " transforms into ", backFace, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, frontCard.getName(), backFace.getName());
            }
        }
    }

    private void checkPendingBoostSourceByDiscardedManaValue(GameData gameData, Card discardedCard) {
        PendingBoostSourceByDiscardedManaValue pending = gameData.pendingBoostSourceByDiscardedManaValue;
        if (pending == null) {
            return;
        }
        gameData.pendingBoostSourceByDiscardedManaValue = null;
        int boost = discardedCard.getManaValue();
        if (boost <= 0) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        if (source == null) {
            return;
        }
        source.setPowerModifier(source.getPowerModifier() + boost);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(source.getCard())
                .text(String.format(" gets +%d/+0 until end of turn.", boost))
                .build());
        log.info("Game {} - {} gets +{}/+0 (discarded card mana value)", gameData.id, source.getCard().getName(), boost);
    }

    private boolean hasEnterBattlefieldOnDiscardEffect(Card card) {
        return card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                .anyMatch(e -> e instanceof EnterBattlefieldOnDiscardEffect);
    }

    private void finalizePendingReturnToHandOnDiscard(GameData gameData) {
        PendingReturnToHandOnDiscardType pending = gameData.pendingReturnToHandOnDiscardType;
        if (pending == null) {
            return;
        }
        // No matching card type was discarded — spell goes to graveyard as normal
        graveyardService.addCardToGraveyard(gameData, pending.controllerId(), pending.card());
        gameData.pendingReturnToHandOnDiscardType = null;
    }
}


