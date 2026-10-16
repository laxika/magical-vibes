package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;

/**
 * Coordinates hand-to-battlefield choices in player order. The normal mode places all chosen
 * cards onto the battlefield as one simultaneous batch; a controller-first non-repeating mode
 * and the repeating mode place each choice immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCardFromHandToBattlefieldSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final AuraAttachmentService auraAttachmentService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    /** Begins the next eligible player's optional choice, or places the accumulated choices. */
    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds,
                                   List<UUID> chosenCardIds,
                                   EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                   String cardName) {
        return beginNextChoice(gameData, orderedPlayerIds, chosenCardIds, effect, cardName, false, null);
    }

    /** Begins a choice round, restarting it when the repeating mode put at least one card. */
    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds,
                                   List<UUID> chosenCardIds,
                                   EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                   String cardName, boolean cardPutThisRound) {
        return beginNextChoice(gameData, orderedPlayerIds, chosenCardIds, effect, cardName,
                cardPutThisRound, null);
    }

    /** Begins a choice round while retaining the controller-first starting player for repeats. */
    public boolean beginNextChoice(GameData gameData, List<UUID> orderedPlayerIds,
                                   List<UUID> chosenCardIds,
                                   EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                   String cardName, boolean cardPutThisRound,
                                   UUID startingPlayerId) {
        for (int i = 0; i < orderedPlayerIds.size(); i++) {
            UUID playerId = orderedPlayerIds.get(i);
            List<UUID> validCardIds = validCardIds(gameData, playerId, effect);
            if (validCardIds.isEmpty()) {
                continue;
            }

            List<UUID> remainingPlayerIds = new ArrayList<>(orderedPlayerIds.subList(i + 1, orderedPlayerIds.size()));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.EachPlayerMayPutCardFromHandChoice(
                    playerId, validCardIds, remainingPlayerIds, chosenCardIds, effect.predicate(),
                    effect.label(), cardName, effect.repeatUntilNoOne(), startingPlayerId, cardPutThisRound,
                    effect.anyNumber()));
            log.info("Game {} - Awaiting {} to choose an {} to put onto the battlefield ({})",
                    gameData.id, gameData.playerIdToName.get(playerId), effect.label(), cardName);
            return true;
        }

        if (effect.repeatUntilNoOne()) {
            if (!cardPutThisRound) {
                return false;
            }
            return beginNextChoice(gameData, roundOrder(gameData, effect, startingPlayerId), List.of(),
                    effect, cardName, false, startingPlayerId);
        }

        putChosenCardsOntoBattlefield(gameData, chosenCardIds, effect, cardName);
        return false;
    }

    /** Moves one selected hand card immediately, as required by Hypergenesis's sequential flow. */
    public boolean putCardOntoBattlefield(GameData gameData, UUID playerId, UUID cardId,
                                          String cardName) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return false;
        }

        Card chosenCard = hand.stream().filter(card -> card.getId().equals(cardId)).findFirst().orElse(null);
        if (chosenCard == null) {
            return false;
        }
        UUID attachmentId = null;
        if (chosenCard.isAura() && !chosenCard.isEnchantZone()) {
            List<UUID> hosts = auraHostIds(gameData, playerId, chosenCard);
            if (hosts.isEmpty()) {
                // An Aura with no legal host stays in its current zone (CR 303.4g).
                log.info("Game {} - {} stays in hand: no legal host", gameData.id, chosenCard.getName());
                return false;
            }
            if (hosts.size() > 1) {
                // CR 303.4f: the entering player chooses what the Aura enchants; the batch asks for the
                // host and then places the Aura, and the process continues once that choice is answered.
                battlefieldEntryBatchSupport.begin(gameData, List.of(
                        new BattlefieldEntryCard(playerId, playerId, chosenCard, Zone.HAND, null)));
                return true;
            }
            attachmentId = hosts.getFirst();
        }
        hand.remove(chosenCard);

        Permanent permanent = new Permanent(chosenCard);
        permanent.setAttachedTo(attachmentId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData), List.of());
        if (gameQueryService.findPermanentController(gameData, permanent.getId()) != null) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, chosenCard, null, false);
        }
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " puts ")
                .card(chosenCard)
                .text(" onto the battlefield (" + cardName + ").")
                .build());
        log.info("Game {} - Put chosen {} card onto the battlefield", gameData.id, chosenCard.getName());
        return true;
    }

    private List<UUID> auraHostIds(GameData gameData, UUID playerId, Card aura) {
        List<UUID> hosts = new ArrayList<>();
        if (!aura.isEnchantPlayer()) {
            gameData.forEachPermanent((controllerId, permanent) -> {
                if (!gameQueryService.cantBeEnchantedByOtherAuras(gameData, permanent)
                        && auraAttachmentService.canEnchant(gameData, aura, playerId, permanent)) {
                    hosts.add(permanent.getId());
                }
            });
        }
        for (UUID hostPlayerId : gameData.orderedPlayerIds) {
            if (auraAttachmentService.canEnchantPlayer(gameData, aura, playerId, hostPlayerId)) {
                hosts.add(hostPlayerId);
            }
        }
        return hosts;
    }

    private List<UUID> validCardIds(GameData gameData, UUID playerId,
                                    EachPlayerMayPutCardFromHandToBattlefieldEffect effect) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null) {
            return List.of();
        }
        return hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.predicate(), card.getId(), gameData, playerId))
                .map(Card::getId)
                .toList();
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayerIds.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(activeIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, activeIndex));
        return rotated;
    }

    private List<UUID> roundOrder(GameData gameData,
                                  EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                  UUID startingPlayerId) {
        if (!effect.startsWithController() || startingPlayerId == null) {
            return apnapOrder(gameData);
        }

        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int startingIndex = orderedPlayerIds.indexOf(startingPlayerId);
        if (startingIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(startingIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, startingIndex));
        return rotated;
    }

    private void putChosenCardsOntoBattlefield(GameData gameData, List<UUID> chosenCardIds,
                                                EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                                String cardName) {
        if (chosenCardIds.isEmpty()) {
            return;
        }

        List<BattlefieldEntryCard> chosenCards = new ArrayList<>();
        for (UUID cardId : chosenCardIds) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Card> hand = gameData.playerHands.get(playerId);
                if (hand == null) continue;
                Card card = hand.stream().filter(candidate -> candidate.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null) {
                    chosenCards.add(new BattlefieldEntryCard(
                            playerId, playerId, card, Zone.HAND, null));
                    break;
                }
            }
        }
        battlefieldEntryBatchSupport.begin(gameData, chosenCards);
    }
}
