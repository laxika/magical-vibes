package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AsEntersDiscardEntry;
import com.github.laxika.magicalvibes.model.effect.DiscardCreatureCardsAsEntersForKeywordCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * "As this permanent enters, discard any number of [matching] cards" replacement choices
 * ({@link DiscardCreatureCardsAsEntersForKeywordCountersEffect}). The choices are made while the
 * permanent is still outside the battlefield: first how many cards to discard, then which ones. The
 * permanent then enters with a keyword counter for each listed keyword found on a discarded card,
 * before any of its enter-the-battlefield abilities are collected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsEntersDiscardSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Autowired @Lazy
    private BattlefieldPlacementService battlefieldPlacementService;
    @Autowired @Lazy
    private BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    @Autowired @Lazy
    private AsEntersInteractionService asEntersInteractionService;
    @Autowired @Lazy
    private InputCompletionService inputCompletionService;

    /**
     * Starts the number-of-cards prompt for an entering permanent that has the replacement.
     *
     * @return {@code true} if an interaction was begun and the entry must wait for the answers
     */
    public boolean begin(GameData gameData, BattlefieldEntryRequest request) {
        Permanent entering = request.permanent();
        if (entering.isAsEntersDiscardResolved()) {
            return false;
        }
        DiscardCreatureCardsAsEntersForKeywordCountersEffect effect = entering.getCard()
                .getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(DiscardCreatureCardsAsEntersForKeywordCountersEffect.class::isInstance)
                .map(DiscardCreatureCardsAsEntersForKeywordCountersEffect.class::cast)
                .findFirst().orElse(null);
        if (effect == null) {
            return false;
        }
        UUID controllerId = request.controllerId();
        List<Integer> validIndices = entering.isFaceDown()
                || gameQueryService.hasLostPrintedAbilitiesAsEntering(gameData, controllerId, entering)
                ? List.of() : validIndices(gameData, controllerId, entering, effect);
        if (validIndices.isEmpty()) {
            entering.setAsEntersDiscardResolved(true);
            return false;
        }
        String cardName = entering.getCard().getName();
        PendingMayAbility carrier = new PendingMayAbility(entering.getCard(), controllerId,
                List.of(new AsEntersDiscardEntry(request, effect)),
                cardName + " — choose how many cards to discard as it enters");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                controllerId, 0, validIndices.size(),
                "Choose how many creature cards to discard for " + cardName + ".", cardName,
                false, null, carrier));
        return true;
    }

    /** Continues the entry after the controller has chosen how many cards to discard. */
    public void handleCount(GameData gameData, Player player, PendingMayAbility carrier, int chosenCount) {
        AsEntersDiscardEntry entry = carrier.effects().stream()
                .filter(AsEntersDiscardEntry.class::isInstance)
                .map(AsEntersDiscardEntry.class::cast)
                .findFirst().orElseThrow();
        BattlefieldEntryRequest request = entry.entryRequest();
        Permanent entering = request.permanent();
        UUID controllerId = request.controllerId();
        List<Integer> validIndices = validIndices(gameData, controllerId, entering, entry.effect());
        int count = Math.min(chosenCount, validIndices.size());
        gameData.interaction.clearAwaitingInput();
        if (count <= 0) {
            complete(gameData, entry, List.of());
            return;
        }
        playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                entering.getCard().getName() + " — choose a creature card to discard as it enters.", count,
                DiscardFollowUp.thenEffectWithEventValueAndDiscardedCards(
                        entering.getCard(), entry, count, null, null, controllerId));
    }

    /** Finishes the replacement once the chosen cards have been discarded and resumes the entry. */
    public void complete(GameData gameData, AsEntersDiscardEntry entry, List<UUID> discardedCardIds) {
        BattlefieldEntryRequest request = entry.entryRequest();
        Permanent entering = request.permanent();
        Set<CounterType> counters = EnumSet.noneOf(CounterType.class);
        for (UUID cardId : discardedCardIds) {
            Card card = findDiscardedCard(gameData, cardId);
            if (card == null) {
                continue;
            }
            for (CounterType counterType : entry.effect().counterTypes()) {
                if (card.getKeywords().contains(counterType.grantedKeyword())) {
                    counters.add(counterType);
                }
            }
        }
        if (!gameQueryService.cantHaveCounters(gameData, entering)) {
            for (CounterType counterType : counters) {
                entering.setCounterCount(counterType, entering.getCounterCount(counterType) + 1);
            }
            if (!counters.isEmpty()) {
                gameLogService.append(gameData, GameLog.cardThen(entering.getCard(),
                        " enters with " + counters.size() + " keyword counter"
                                + (counters.size() == 1 ? "" : "s") + "."));
            }
        }
        entering.setAsEntersDiscardResolved(true);

        if (battlefieldEntryBatchSupport.completeNativeChoice(gameData, request)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        battlefieldPlacementService.place(gameData, request);
        var spell = request.sourceStackEntry();
        asEntersInteractionService.handleCreatureEnteredBattlefield(gameData, request.controllerId(),
                entering.getCard(), spell == null ? null : spell.getTargetId(),
                entering.getCastFromZone() == Zone.HAND,
                spell != null && spell.getEtbMode() != null ? spell.getEtbMode() : request.xValue(),
                request.xValue(), request.kicked(), spell == null ? List.of() : spell.getTargetIds(),
                request.repeatedAdditionalCosts(),
                spell == null ? List.of() : spell.getConvokeCreatureIds());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private List<Integer> validIndices(GameData gameData, UUID controllerId, Permanent entering,
                                       DiscardCreatureCardsAsEntersForKeywordCountersEffect effect) {
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.getId().equals(entering.getCard().getId())
                    && predicateEvaluationService.matchesCardPredicate(
                    card, effect.cardFilter(), entering.getCard().getId())) {
                indices.add(i);
            }
        }
        return indices;
    }

    /** A discarded card is in its owner's graveyard, or in exile when a replacement exiled it. */
    private Card findDiscardedCard(GameData gameData, UUID cardId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (card != null) {
            return card;
        }
        var exiled = gameData.findExiledCard(cardId);
        return exiled == null ? null : exiled.card();
    }
}
