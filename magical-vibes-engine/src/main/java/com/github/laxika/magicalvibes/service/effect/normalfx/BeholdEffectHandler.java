package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BeholdEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BeholdEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BeholdEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BeholdEffect behold = (BeholdEffect) effect;
        List<UUID> validCardIds = matchingObjectIds(gameData, entry.getControllerId(), behold.subtype());
        if (validCardIds.isEmpty()) {
            return;
        }

        String subtypeName = behold.subtype().getDisplayName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.BeholdChoice(
                entry.getControllerId(), validCardIds,
                "Choose a " + subtypeName + " you control or reveal a " + subtypeName
                        + " card from your hand to behold it.", behold));
    }

    public void completeChoice(GameData gameData, UUID chosenCardId,
                               PendingInteraction.BeholdChoice interaction) {
        Permanent chosenPermanent = findMatchingPermanent(
                gameData, interaction.playerId(), chosenCardId, interaction.effect().subtype());
        Card chosenHandCard = chosenPermanent == null
                ? findMatchingHandCard(gameData, interaction.playerId(), chosenCardId,
                        interaction.effect().subtype())
                : null;
        if (chosenPermanent == null && chosenHandCard == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        if (chosenPermanent != null) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " chooses ", chosenPermanent.getCard(), " to behold it."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " reveals ", chosenHandCard, " from their hand to behold it."));
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry != null) {
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                    List.of(interaction.effect().thenEffect()));
        }
    }

    private List<UUID> matchingObjectIds(GameData gameData, UUID playerId, CardSubtype subtype) {
        List<UUID> ids = new ArrayList<>();
        PermanentHasSubtypePredicate permanentPredicate = new PermanentHasSubtypePredicate(subtype);
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, permanentPredicate)) {
                ids.add(permanent.getCard().getId());
            }
        }
        CardSubtypePredicate cardPredicate = new CardSubtypePredicate(subtype);
        for (Card card : gameData.playerHands.getOrDefault(playerId, List.of())) {
            if (predicateEvaluationService.matchesCardPredicate(
                    card, cardPredicate, card.getId(), gameData, playerId)) {
                ids.add(card.getId());
            }
        }
        return ids;
    }

    private Permanent findMatchingPermanent(GameData gameData, UUID playerId, UUID cardId,
                                            CardSubtype subtype) {
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(subtype);
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (permanent.getCard().getId().equals(cardId)
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                return permanent;
            }
        }
        return null;
    }

    private Card findMatchingHandCard(GameData gameData, UUID playerId, UUID cardId,
                                      CardSubtype subtype) {
        CardSubtypePredicate predicate = new CardSubtypePredicate(subtype);
        for (Card card : gameData.playerHands.getOrDefault(playerId, List.of())) {
            if (card.getId().equals(cardId)
                    && predicateEvaluationService.matchesCardPredicate(
                    card, predicate, card.getId(), gameData, playerId)) {
                return card;
            }
        }
        return null;
    }
}
