package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lets a test name the targets and division of the next divided triggered ability before it triggers, as a
 * shorthand for answering that ability's prompts.
 *
 * <p>A divided triggered ability announces its targets and division as it is put on the stack (CR 601.2d, 603.3d),
 * so the engine never takes a division before then. The harness keeps each declared division until a divided
 * ability asks for one and answers its prompts with it: the target choice of an ability announced on the stack
 * ({@code MultiPermanentChoiceContext.StackedDividedDamageTargets}, the declared targets in declaration order) and
 * each per-target amount of the division ({@code ChoiceContext.CounterDistributionAssignment}, as a list or number
 * choice). The targets of an ability whose card declares target groups are chosen through the ETB target prompts,
 * which the cast helpers' target list answers. Declarations are used in order, one per announced division.</p>
 */
final class DeclaredDivisions {

    private static final int ANSWER_LIMIT = 1000;

    private final GameData gameData;
    private final GameService gameService;
    private final List<Player> players;
    private final Deque<Map<UUID, Integer>> declarations = new ArrayDeque<>();

    DeclaredDivisions(GameData gameData, GameService gameService, List<Player> players) {
        this.gameData = gameData;
        this.gameService = gameService;
        this.players = players;
    }

    /** Declares the next announced division; an empty map chooses no targets. */
    void declare(Map<UUID, Integer> division) {
        declarations.addLast(new LinkedHashMap<>(division));
    }

    /**
     * Answers every open prompt of a declared division.
     *
     * @return whether a prompt was answered
     */
    boolean answerOpenPrompts() {
        boolean answeredAny = false;
        for (int answered = 0; answered < ANSWER_LIMIT && !declarations.isEmpty(); answered++) {
            if (!gameData.interaction.isAwaitingInput() || !answerActivePrompt()) {
                return answeredAny;
            }
            answeredAny = true;
        }
        return answeredAny;
    }

    private boolean answerActivePrompt() {
        PendingInteraction active = gameData.interaction.activeInteraction();
        Map<UUID, Integer> division = declarations.peekFirst();
        if (active instanceof PendingInteraction.MultiPermanentChoice choice
                && choice.context() instanceof MultiPermanentChoiceContext.StackedDividedDamageTargets) {
            answer(choice.playerId(), new InteractionAnswer.PermanentsChosen(List.copyOf(division.keySet())));
            if (division.size() <= 1) {
                declarations.removeFirst();
            }
            return true;
        }
        ChoiceContext.CounterDistributionAssignment distribution = null;
        UUID playerId = null;
        boolean listChoice = false;
        if (active instanceof PendingInteraction.ColorChoice choice
                && choice.context() instanceof ChoiceContext.CounterDistributionAssignment assignment) {
            distribution = assignment;
            playerId = choice.playerId();
            listChoice = true;
        } else if (active instanceof PendingInteraction.XValueChoice choice && choice.distribution() != null) {
            distribution = choice.distribution();
            playerId = choice.playerId();
        }
        if (distribution == null) {
            return false;
        }
        UUID target = distribution.targetIds().get(distribution.nextTargetIndex());
        Integer amount = division.get(target);
        if (amount == null) {
            throw new IllegalStateException("The declared division " + division + " names no amount for " + target);
        }
        if (distribution.nextTargetIndex() == distribution.targetIds().size() - 1) {
            declarations.removeFirst();
        }
        answer(playerId, listChoice
                ? new InteractionAnswer.ListChoiceMade(String.valueOf(amount))
                : new InteractionAnswer.NumberChosen(amount));
        return true;
    }

    private void answer(UUID playerId, InteractionAnswer answer) {
        Player player = players.stream().filter(candidate -> candidate.getId().equals(playerId))
                .findFirst().orElseThrow();
        gameService.handleInteractionAnswer(gameData, player, answer);
    }
}
