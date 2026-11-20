package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * "You may exile a [creature] card from a graveyard." Gathers every matching card in every graveyard and asks the
 * controller to choose up to one (exactly one when mandatory). The answer is recorded by
 * {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} through
 * {@code graveyardTargetOperation.resolutionTimeExileThenEffectResume}, and this handler resolves again with it before
 * anything changed: it exiles the chosen card (tracked with the source permanent when the effect says so) and records
 * the EXILE step (NONE when nothing was exiled), so a later step of the same resolution can read it.
 */
@Component
@RequiredArgsConstructor
public class ExileUpToOneCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToOneCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileUpToOneCardFromGraveyardEffect) effect;
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);

        if (state.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileThenEffectChosenCardId;
            state.resolutionTimeExileThenEffectChoiceMade = false;
            state.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            if (chosenCardId == null) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " chooses not to exile a card from a graveyard."));
                return;
            }
            Card chosen = matchingCards(gameData, entry, exileEffect).stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .orElse(null);
            UUID ownerId = chosen == null ? null : gameQueryService.findGraveyardOwnerById(gameData, chosenCardId);
            if (chosen == null || ownerId == null
                    || !graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, chosenCardId, chosen,
                    exileEffect.trackWithSource() ? entry.getSourcePermanentId() : null)) {
                return;
            }
            entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(chosenCardId), List.of(ownerId)));
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " exiles ", chosen, " from a graveyard."));
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileEffect);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    exileEffect.filter() == null
                            ? " finds no cards in any graveyard to exile."
                            : " finds no matching card in any graveyard to exile."));
            return;
        }

        state.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates, 1,
                exileEffect.mandatory() ? 1 : 0,
                exileEffect.mandatory()
                        ? "Choose one card from a graveyard to exile."
                        : "Choose up to one card from a graveyard to exile.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry, ExileUpToOneCardFromGraveyardEffect effect) {
        List<Card> candidates = new ArrayList<>();
        UUID sourceCardId = entry.getCard().getId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                if (effect.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), sourceCardId, gameData, playerId)) {
                    candidates.add(card);
                }
            }
        }
        return candidates;
    }
}
