package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Asks which of the named cards are chosen and resolves again with each answer, before anything changed: the answer
 * is recorded by {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen}. Records {@link StepKind#CHOOSE}
 * after the last answer.
 */
@Component
@RequiredArgsConstructor
public class ChooseCardEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChooseCardEffect) effect;
        List<LocatedCard> candidates = subjectResolutionService.locatedCards(gameData, entry, effect, choose.cards());
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        List<UUID> picked = new ArrayList<>();
        int choice = 0;
        if (state.chooseCardAnswer != null) {
            picked.addAll(state.chooseCardPicked);
            picked.addAll(state.chooseCardAnswer);
            choice = state.chooseCardIndex + 1;
            state.chooseCardAnswer = null;
            state.chooseCardPicked = List.of();
            state.chooseCardIndex = 0;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }
        int choices = choose.each().isEmpty() ? 1 : choose.each().size();
        for (; choice < choices; choice++) {
            ChooseCardEffect.Each each = choose.each().isEmpty() ? null : choose.each().get(choice);
            List<LocatedCard> offered = candidates.stream()
                    // Choosing a card again where none has to be chosen is choosing none.
                    .filter(located -> choose.upTo() || !picked.contains(located.card().getId()))
                    .filter(located -> each == null || matches(gameData, entry, located, each))
                    .toList();
            int wanted = each != null ? 1 : amountEvaluationService.evaluate(gameData, choose.count(),
                    AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()));
            int most = Math.min(wanted, offered.size());
            if (most <= 0) {
                continue;
            }
            if (!choose.upTo() && !choose.alwaysAsked() && offered.size() == most) {
                offered.forEach(located -> picked.add(located.card().getId()));
                continue;
            }
            // The step records as it resolves again with the answer: until then the candidates are read as they are
            // now, and may be what the CHOOSE step before this one chose.
            state.chooseCardResume = true;
            state.chooseCardPicked = List.copyOf(picked);
            state.chooseCardIndex = choice;
            gameData.rerunCurrentEffectAfterInteraction = true;
            String what = each == null ? choose.label() : each.label();
            String prompt = !choose.upTo() ? "Choose " + what + "."
                    : most == 1 ? "You may choose " + what + "." : "Choose up to " + most + ": " + what + ".";
            playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                    offered.stream().map(LocatedCard::card).toList(), most, choose.upTo() ? 0 : most, prompt);
            return;
        }
        record(gameData, entry, candidates.stream()
                .filter(located -> picked.contains(located.card().getId())).toList());
    }

    private boolean matches(GameData gameData, StackEntry entry, LocatedCard located, ChooseCardEffect.Each each) {
        Card judged = gameData.isExiledFaceDown(located.card().getId())
                ? Card.namedRuntimePlaceholder("") : located.card();
        return predicateEvaluationService.matchesCardPredicate(
                judged, each.filter(), entry.getCard().getId(), gameData, located.ownerId());
    }

    private void record(GameData gameData, StackEntry entry, List<LocatedCard> chosen) {
        List<UUID> cardIds = new ArrayList<>();
        List<UUID> ownerIds = new ArrayList<>();
        List<MovedCard> moved = new ArrayList<>();
        for (LocatedCard card : chosen) {
            UUID cardId = card.card().getId();
            cardIds.add(cardId);
            if (card.ownerId() != null && !ownerIds.contains(card.ownerId())) {
                ownerIds.add(card.ownerId());
            }
            moved.add(gameData.boundToStay(new MovedCard(cardId, cardId, card.zone(), card.ownerId())));
        }
        entry.recordStep(StepKind.CHOOSE,
                cardIds.isEmpty() ? StepOutcome.NONE : StepOutcome.ofObjects(cardIds, ownerIds, moved));
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        for (LocatedCard card : chosen) {
            gameLogService.append(gameData, gameData.isExiledFaceDown(card.card().getId())
                    ? GameLog.text(playerName + " chooses a face-down card in exile.")
                    : GameLog.textCardText(playerName + " chooses ", card.card(), "."));
        }
    }
}
