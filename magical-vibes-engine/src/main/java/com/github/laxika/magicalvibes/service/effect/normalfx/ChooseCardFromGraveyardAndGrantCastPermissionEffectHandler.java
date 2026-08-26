package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndGrantCastPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardFromGraveyardAndGrantCastPermissionEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromGraveyardAndGrantCastPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (ChooseCardFromGraveyardAndGrantCastPermissionEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (entry.getTargetId() != null && !gameData.playerIds.contains(entry.getTargetId())) {
            grantPermission(gameData, entry, choiceEffect, entry.getTargetId());
            return;
        }

        List<Card> matchingCards = new ArrayList<>();
        for (UUID playerId : choiceEffect.scope().graveyardOwners(gameData.orderedPlayerIds, controllerId)) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            matchingCards.addAll(graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, choiceEffect.filter(), entry.getCard().getId()))
                    .toList());
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " — no matching card in the graveyard."));
            return;
        }

        if (matchingCards.size() == 1) {
            grantPermission(gameData, entry, choiceEffect, matchingCards.getFirst().getId());
            return;
        }

        // The selected card ID is carried through StackEntry.targetId so the shared graveyard
        // choice completion can resume this parked effect. It is an internal reference, not a
        // rules target, so suppress target-choice triggers while the entry is resumed.
        entry.setNonTargeting(true);
        gameData.resolvedMayTargetingEntry = entry;
        gameData.rerunCurrentEffectAfterInteraction = true;
        List<Integer> validIndices = IntStream.range(0, matchingCards.size()).boxed().toList();
        String graveyardText = choiceEffect.scope() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                ? "your graveyard"
                : "a graveyard";
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, validIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose an instant or sorcery card from " + graveyardText
                                + " to cast this turn.")
                .cardPool(matchingCards)
                .mandatory(true)
                .build());
    }

    private void grantPermission(GameData gameData, StackEntry entry,
                                 ChooseCardFromGraveyardAndGrantCastPermissionEffect effect,
                                 UUID cardId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID controllerId = entry.getControllerId();
        UUID graveyardOwnerId = card == null
                ? null
                : gameQueryService.findGraveyardOwnerById(gameData, cardId);
        boolean validScope = graveyardOwnerId != null
                && effect.scope().graveyardOwners(gameData.orderedPlayerIds, controllerId)
                .contains(graveyardOwnerId);
        if (card == null || !validScope
                || !predicateEvaluationService.matchesCardPredicate(card, effect.filter(), entry.getCard().getId())) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (the chosen card is no longer legal)."));
            return;
        }

        if (effect.withoutPayingManaCost()) {
            gameData.graveyardCardCastPermissionsUntilEndOfTurn.put(cardId,
                    new GameData.GraveyardCardCastPermission(
                            entry.getSourcePermanentId(), controllerId, false,
                            effect.exileInsteadOfGraveyard(), true));
        } else {
            gameData.graveyardPlayPermissions.put(cardId, controllerId);
            gameData.graveyardPlayPermissionsExpireEndOfTurn.add(cardId);
            if (effect.exileInsteadOfGraveyard()) {
                gameData.exileInsteadOfGraveyard.add(cardId);
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " may cast ")
                .card(card)
                .text(" from their graveyard this turn.")
                .build());
        log.info("Game {} - {} may cast {} from their graveyard this turn",
                gameData.id, gameData.playerIdToName.get(controllerId), card.getName());
    }
}
