package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Zone;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.amount.Fixed;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        boolean hidden = choose.cards().inHiddenZone();
        if (choose.chooser() == null) {
            chooseAtRandom(gameData, entry, choose, candidates, hidden);
            return;
        }
        UUID chooserId = subjectResolutionService.player(gameData, entry, effect, choose.chooser());
        if (chooserId == null) {
            candidates = List.of();
        } else if (hidden) {
            // A player chooses among, and is shown, only their own hand (CR 402.3).
            candidates = candidates.stream().filter(located -> chooserId.equals(located.ownerId())).toList();
        }
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
        List<UUID> owners = choose.fromEach() ? ownersInTurnOrder(gameData, candidates) : List.of();
        int choices = choose.fromEach() ? owners.size() : choose.each().isEmpty() ? 1 : choose.each().size();
        for (; choice < choices; choice++) {
            ChooseCardEffect.Each each = choose.each().isEmpty() ? null : choose.each().get(choice);
            UUID owner = choose.fromEach() ? owners.get(choice) : null;
            List<LocatedCard> offered = candidates.stream()
                    // Choosing a card again where none has to be chosen is choosing none.
                    .filter(located -> choose.upTo() && !choose.offeredOnce()
                            || !picked.contains(located.card().getId()))
                    .filter(located -> each == null || matches(gameData, entry, located, each))
                    .filter(located -> owner == null || owner.equals(located.ownerId()))
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
            String what = each != null ? each.label() : owner == null ? choose.label()
                    : choose.label() + " (" + gameData.playerIdToName.get(owner) + "'s)";
            String prompt = !choose.upTo() ? "Choose " + what + "."
                    : most == 1 ? "You may choose " + what + "." : "Choose up to " + most + ": " + what + ".";
            playerInputService.beginMultiGraveyardChoice(gameData, chooserId,
                    offered.stream().map(LocatedCard::card).toList(), most, choose.upTo() ? 0 : most, prompt);
            return;
        }
        record(gameData, entry, choose, candidates.stream()
                .filter(located -> picked.contains(located.card().getId())).toList(), chooserId, hidden);
    }

    /** The owners of {@code candidates}, in turn order: "from each graveyard" is one choice per owner. */
    private static List<UUID> ownersInTurnOrder(GameData gameData, List<LocatedCard> candidates) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> candidates.stream().anyMatch(located -> playerId.equals(located.ownerId())))
                .toList();
    }

    private void chooseAtRandom(GameData gameData, StackEntry entry, ChooseCardEffect choose,
                                List<LocatedCard> candidates, boolean hidden) {
        int wanted = amountEvaluationService.evaluate(gameData, choose.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()));
        List<LocatedCard> drawn = new ArrayList<>(candidates);
        Collections.shuffle(drawn, ThreadLocalRandom.current());
        record(gameData, entry, choose,
                List.copyOf(drawn.subList(0, Math.clamp(wanted, 0, drawn.size()))), null, hidden);
    }

    /**
     * A choice among cards in a hand or library that nothing revealed (CR 400.2), or among cards outside the game: one
     * line for each hand, library or owner chosen from, which names no card and counts none but as the instruction does ("a card", "cards"); the step that
     * moves the cards says what everybody sees.
     */
    private void logHiddenChoice(GameData gameData, ChooseCardEffect choose, List<LocatedCard> chosen,
                                 UUID chooserId) {
        boolean one = choose.each().isEmpty()
                && choose.count() instanceof Fixed fixed && fixed.value() == 1;
        Set<String> lines = new LinkedHashSet<>();
        for (LocatedCard card : chosen) {
            String how = chooserId == null ? " chosen at random." : " chosen.";
            if (card.zone() == Zone.OUTSIDE_GAME) {
                String owner = gameData.playerIdToName.get(card.ownerId());
                lines.add(one ? "A card " + owner + " owns outside the game is" + how
                        : "Cards " + owner + " owns outside the game are" + how);
                continue;
            }
            String where = gameData.playerIdToName.get(card.ownerId()) + "'s "
                    + (card.zone() == Zone.HAND ? "hand" : "library");
            lines.add(one ? "A card in " + where + " is" + how : "Cards in " + where + " are" + how);
        }
        lines.forEach(line -> gameLogService.append(gameData, GameLog.text(line)));
    }

    private boolean matches(GameData gameData, StackEntry entry, LocatedCard located, ChooseCardEffect.Each each) {
        Card judged = gameData.isExiledFaceDown(located.card().getId())
                ? Card.namedRuntimePlaceholder("") : located.card();
        return predicateEvaluationService.matchesCardPredicate(
                judged, each.filter(), entry.getCard().getId(), gameData, located.ownerId());
    }

    private void record(GameData gameData, StackEntry entry, ChooseCardEffect choose, List<LocatedCard> chosen,
                        UUID chooserId, boolean hidden) {
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
        if (hidden) {
            logHiddenChoice(gameData, choose, chosen, chooserId);
            return;
        }
        // A card an earlier step put into a hand ("one of the conjured cards", "one of those drafted cards") is as hidden
        // as the hand it is in (CR 400.2): its choice names no card.
        List<LocatedCard> inHand = chosen.stream().filter(card -> card.zone() == Zone.HAND).toList();
        if (!inHand.isEmpty()) {
            logHiddenChoice(gameData, choose, inHand, chooserId);
        }
        for (LocatedCard card : chosen) {
            if (card.zone() == Zone.HAND) {
                continue;
            }
            if (chooserId == null) {
                gameLogService.append(gameData, gameData.isExiledFaceDown(card.card().getId())
                        ? GameLog.text("A face-down card in exile is chosen at random.")
                        : GameLog.cardThen(card.card(), " is chosen at random."));
            } else {
                String playerName = gameData.playerIdToName.get(chooserId);
                gameLogService.append(gameData, gameData.isExiledFaceDown(card.card().getId())
                        ? GameLog.text(playerName + " chooses a face-down card in exile.")
                        : GameLog.textCardText(playerName + " chooses ", card.card(), "."));
            }
        }
    }
}
