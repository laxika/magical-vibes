package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * "You may exile a [creature] card from a graveyard." Gathers every matching card in every graveyard, or in those of
 * the players the effect names, and asks the controller to choose up to one (exactly one when mandatory, unasked when
 * it is the only one in their own graveyard). The answer is recorded by
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
    private final ExileService exileService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

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
            if (chosenCardId == null) {
                String playerName = gameData.playerIdToName.get(entry.getControllerId());
                gameLogService.append(gameData, GameLog.text(
                        playerName + " chooses not to exile a card from a graveyard."));
                return;
            }
            matchingCards(gameData, entry, exileEffect).stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .ifPresent(chosen -> exile(gameData, entry, exileEffect, chosen));
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileEffect);
        if (candidates.isEmpty()) {
            String where = exileEffect.graveyardOf() == null ? "any graveyard" : "that graveyard";
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    exileEffect.filter() == null
                            ? " finds no cards in " + where + " to exile."
                            : " finds no matching card in " + where + " to exile."));
            return;
        }

        // "Exile a card from your graveyard" with one card to exile leaves nothing to choose.
        if (exileEffect.mandatory() && candidates.size() == 1
                && exileEffect.graveyardOf() instanceof PlayerSubject.You) {
            exile(gameData, entry, exileEffect, candidates.getFirst());
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

    private void exile(GameData gameData, StackEntry entry, ExileUpToOneCardFromGraveyardEffect effect, Card card) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        List<LastKnownCardStats> known = gameQueryService.asInGraveyard(gameData, List.of(card));
        if (ownerId == null) {
            return;
        }
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        if (effect.trackWithSource() && entry.getSourcePermanentId() != null) {
            exileService.exileCard(gameData, ownerId, card, entry.getSourcePermanentId());
        } else {
            exileService.exileCard(gameData, ownerId, card);
        }
        entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(card.getId()), List.of(ownerId))
                .withLastKnownCards(known));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", card, " from a graveyard."));
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry, ExileUpToOneCardFromGraveyardEffect effect) {
        List<Card> candidates = new ArrayList<>();
        UUID sourceCardId = entry.getCard().getId();
        List<UUID> graveyardPlayerIds = effect.graveyardOf() == null ? gameData.orderedPlayerIds
                : subjectResolutionService.players(gameData, entry, effect, effect.graveyardOf());
        for (UUID playerId : graveyardPlayerIds) {
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
