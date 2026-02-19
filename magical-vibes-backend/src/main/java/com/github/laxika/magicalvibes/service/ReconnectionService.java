package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromLibraryMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseHandTopBottomMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconnectionService {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final CombatService combatService;
    private final GameQueryService gameQueryService;

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        if (gameData.awaitingInput == null) return;

        switch (gameData.awaitingInput) {
            case ATTACKER_DECLARATION -> {
                if (playerId.equals(gameData.activePlayerId)) {
                    List<Integer> attackable = combatService.getAttackableCreatureIndices(gameData, playerId);
                    List<Integer> mustAttack = combatService.getMustAttackIndices(gameData, playerId, attackable);
                    sessionManager.sendToPlayer(playerId, new AvailableAttackersMessage(attackable, mustAttack));
                }
            }
            case BLOCKER_DECLARATION -> {
                UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
                if (playerId.equals(defenderId)) {
                    List<Integer> blockable = combatService.getBlockableCreatureIndices(gameData, defenderId);
                    List<Integer> attackerIndices = combatService.getAttackingCreatureIndices(gameData, gameData.activePlayerId);
                    List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                    attackerIndices = attackerIndices.stream()
                            .filter(idx -> !attackerBattlefield.get(idx).isCantBeBlocked()
                                    && attackerBattlefield.get(idx).getCard().getEffects(EffectSlot.STATIC).stream()
                                            .noneMatch(e -> e instanceof CantBeBlockedEffect))
                            .toList();
                    sessionManager.sendToPlayer(defenderId, new AvailableBlockersMessage(blockable, attackerIndices));
                }
            }
            case CARD_CHOICE, TARGETED_CARD_CHOICE -> {
                if (playerId.equals(gameData.awaitingCardChoicePlayerId)) {
                    sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                            new ArrayList<>(gameData.awaitingCardChoiceValidIndices), "Choose a card from your hand."));
                }
            }
            case DISCARD_CHOICE -> {
                if (playerId.equals(gameData.awaitingCardChoicePlayerId)) {
                    sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                            new ArrayList<>(gameData.awaitingCardChoiceValidIndices), "Choose a card to discard."));
                }
            }
            case ACTIVATED_ABILITY_DISCARD_COST_CHOICE -> {
                if (playerId.equals(gameData.awaitingCardChoicePlayerId) && gameData.pendingAbilityActivation != null) {
                    String typeName = gameData.pendingAbilityActivation.discardCostType().name().toLowerCase();
                    sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(
                            new ArrayList<>(gameData.awaitingCardChoiceValidIndices),
                            "Choose a " + typeName + " card to discard as an activation cost."));
                }
            }
            case PERMANENT_CHOICE -> {
                if (playerId.equals(gameData.awaitingPermanentChoicePlayerId)) {
                    sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(
                            new ArrayList<>(gameData.awaitingPermanentChoiceValidIds), "Choose a permanent."));
                }
            }
            case GRAVEYARD_CHOICE -> {
                if (playerId.equals(gameData.awaitingGraveyardChoicePlayerId)) {
                    sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(
                            new ArrayList<>(gameData.awaitingGraveyardChoiceValidIndices), "Choose a card from the graveyard."));
                }
            }
            case COLOR_CHOICE -> {
                if (playerId.equals(gameData.awaitingColorChoicePlayerId)) {
                    List<String> options;
                    String prompt;
                    if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeFromWord) {
                        options = new ArrayList<>();
                        options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
                        options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
                        prompt = "Choose a color word or basic land type to replace.";
                    } else if (gameData.colorChoiceContext instanceof ColorChoiceContext.TextChangeToWord ctx) {
                        if (ctx.isColor()) {
                            options = GameQueryService.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(ctx.fromWord())).toList();
                            prompt = "Choose the replacement color word.";
                        } else {
                            options = GameQueryService.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(ctx.fromWord())).toList();
                            prompt = "Choose the replacement basic land type.";
                        }
                    } else {
                        options = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                        prompt = "Choose a color.";
                    }
                    sessionManager.sendToPlayer(playerId, new ChooseColorMessage(options, prompt));
                }
            }
            case MAY_ABILITY_CHOICE -> {
                if (playerId.equals(gameData.awaitingMayAbilityPlayerId) && !gameData.pendingMayAbilities.isEmpty()) {
                    PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
                    sessionManager.sendToPlayer(playerId, new MayAbilityMessage(next.description()));
                }
            }
            case MULTI_PERMANENT_CHOICE -> {
                if (playerId.equals(gameData.awaitingMultiPermanentChoicePlayerId)) {
                    sessionManager.sendToPlayer(playerId, new ChooseMultiplePermanentsMessage(
                            new ArrayList<>(gameData.awaitingMultiPermanentChoiceValidIds),
                            gameData.awaitingMultiPermanentChoiceMaxCount, "Choose permanents."));
                }
            }
            case MULTI_GRAVEYARD_CHOICE -> {
                if (playerId.equals(gameData.awaitingMultiGraveyardChoicePlayerId)) {
                    List<UUID> validCardIds = new ArrayList<>(gameData.awaitingMultiGraveyardChoiceValidCardIds);
                    List<CardView> cardViews = new ArrayList<>();
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Card> graveyard = gameData.playerGraveyards.get(pid);
                        if (graveyard == null) continue;
                        for (Card card : graveyard) {
                            if (gameData.awaitingMultiGraveyardChoiceValidCardIds.contains(card.getId())) {
                                cardViews.add(cardViewFactory.create(card));
                            }
                        }
                    }
                    sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(
                            validCardIds, cardViews, gameData.awaitingMultiGraveyardChoiceMaxCount,
                            "Exile up to " + gameData.awaitingMultiGraveyardChoiceMaxCount + " cards from graveyards."));
                }
            }
            case LIBRARY_REORDER -> {
                if (playerId.equals(gameData.awaitingLibraryReorderPlayerId) && gameData.awaitingLibraryReorderCards != null) {
                    List<CardView> cardViews = gameData.awaitingLibraryReorderCards.stream().map(cardViewFactory::create).toList();
                    sessionManager.sendToPlayer(playerId, new ReorderLibraryCardsMessage(
                            cardViews, "Put these cards back on top of your library in any order (top to bottom)."));
                }
            }
            case LIBRARY_SEARCH -> {
                if (playerId.equals(gameData.awaitingLibrarySearchPlayerId) && gameData.awaitingLibrarySearchCards != null) {
                    List<CardView> cardViews = gameData.awaitingLibrarySearchCards.stream().map(cardViewFactory::create).toList();
                    String prompt = gameData.awaitingLibrarySearchCanFailToFind
                            ? "Search your library for a basic land card to put into your hand."
                            : "Search your library for a card to put into your hand.";
                    sessionManager.sendToPlayer(playerId, new ChooseCardFromLibraryMessage(
                            cardViews, prompt, gameData.awaitingLibrarySearchCanFailToFind));
                }
            }
            case HAND_TOP_BOTTOM_CHOICE -> {
                if (playerId.equals(gameData.awaitingHandTopBottomPlayerId) && gameData.awaitingHandTopBottomCards != null) {
                    List<CardView> cardViews = gameData.awaitingHandTopBottomCards.stream().map(cardViewFactory::create).toList();
                    int count = gameData.awaitingHandTopBottomCards.size();
                    sessionManager.sendToPlayer(playerId, new ChooseHandTopBottomMessage(
                            cardViews, "Look at the top " + count + " cards of your library. Choose one to put into your hand."));
                }
            }
            case REVEALED_HAND_CHOICE -> {
                if (playerId.equals(gameData.awaitingCardChoicePlayerId) && gameData.awaitingRevealedHandChoiceTargetPlayerId != null) {
                    UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
                    List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
                    String targetName = gameData.playerIdToName.get(targetPlayerId);
                    List<CardView> cardViews = targetHand.stream().map(cardViewFactory::create).toList();
                    List<Integer> validIndices = new ArrayList<>(gameData.awaitingCardChoiceValidIndices);
                    sessionManager.sendToPlayer(playerId, new ChooseFromRevealedHandMessage(
                            cardViews, validIndices, "Choose a card to put on top of " + targetName + "'s library."));
                }
            }
        }
    }
}
