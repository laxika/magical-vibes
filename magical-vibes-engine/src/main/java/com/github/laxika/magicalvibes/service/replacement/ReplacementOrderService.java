package com.github.laxika.magicalvibes.service.replacement;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.replacement.ReplacementEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies the replacement and prevention effects that modify an event in the order CR 616.1
 * requires. While two or more apply, the event's chooser picks the next one through a
 * {@link ChoiceContext.ReplacementEffectOrder} prompt; after each application the applicable set
 * is collected again (CR 616.1f), excluding every effect already applied (CR 614.5).
 * Self-replacement effects go first (CR 616.1a).
 *
 * <p>No prompt is shown when a single effect applies, or when every applicable effect is
 * {@linkplain ReplacementOption#pure() pure} and every possible order — including effects that
 * only become applicable along the way (CR 616.2) — ends in the same event. Those effects are then
 * applied in collection order, because no choice could change anything.
 */
@Service
public class ReplacementOrderService {

    static final String PROMPT = "Choose the replacement or prevention effect to apply next.";
    private static final int MAX_PREVIEW_LEAVES = 720;

    private final List<ReplacementCandidateSource<?>> sources;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public ReplacementOrderService(@Lazy List<ReplacementCandidateSource<?>> sources,
                                   @Lazy InteractionHandlerRegistry interactionHandlerRegistry) {
        this.sources = sources;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    /** What became of an event handed to {@link #run} or {@link #answer}. */
    public sealed interface Outcome<E extends ReplacementEvent> {

        /** Every applicable effect has been applied; {@code event} is what actually happens. */
        record Completed<E extends ReplacementEvent>(E event) implements Outcome<E> {}

        /** The chooser is being asked which effect to apply next. */
        record Prompted<E extends ReplacementEvent>() implements Outcome<E> {}

        /**
         * An applied effect is waiting for a decision of its own; {@code event}, with that effect
         * already applied, continues through {@link #run} afterwards.
         */
        record Suspended<E extends ReplacementEvent>(E event) implements Outcome<E> {}
    }

    private record Candidate<E extends ReplacementEvent>(ReplacementCandidateSource<E> source,
                                                         ReplacementOption option) {}

    /** Applies every effect that modifies {@code event}, prompting whenever the order matters. */
    public <E extends ReplacementEvent> Outcome<E> run(GameData gameData, E event) {
        E current = event;
        while (true) {
            List<Candidate<E>> candidates = applicable(gameData, current);
            if (candidates.isEmpty()) {
                return new Outcome.Completed<>(current);
            }
            if (candidates.size() > 1 && !orderIsIrrelevant(gameData, current, candidates)) {
                prompt(gameData, current, candidates);
                return new Outcome.Prompted<>();
            }
            switch (applyCandidate(gameData, current, candidates.getFirst())) {
                case ReplacementCandidateSource.ApplyResult.Applied<E> applied -> current = applied.event();
                case ReplacementCandidateSource.ApplyResult.Suspended<E> suspended -> {
                    return new Outcome.Suspended<>(suspended.event());
                }
            }
        }
    }

    /** Applies the effect the chooser picked from {@code order}, then continues the event. */
    @SuppressWarnings("unchecked")
    public <E extends ReplacementEvent> Outcome<E> answer(GameData gameData,
                                                          ChoiceContext.ReplacementEffectOrder order,
                                                          String label) {
        E event = (E) order.event();
        Candidate<E> chosen = applicable(gameData, event).stream()
                .filter(candidate -> candidate.option().label().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid replacement effect: " + label));
        return switch (applyCandidate(gameData, event, chosen)) {
            case ReplacementCandidateSource.ApplyResult.Applied<E> applied -> run(gameData, applied.event());
            case ReplacementCandidateSource.ApplyResult.Suspended<E> suspended ->
                    new Outcome.Suspended<>(suspended.event());
        };
    }

    /**
     * Every distinct final event {@code event} could become depending on the chooser's decisions,
     * computed without changing the game. Effects that are not pure count as applied without
     * modifying the event. The returned events carry no applied keys.
     */
    public <E extends ReplacementEvent> Set<E> possibleOutcomes(GameData gameData, E event) {
        Set<E> outcomes = new LinkedHashSet<>();
        collectOutcomes(gameData, event, outcomes, new int[]{0});
        return outcomes;
    }

    private <E extends ReplacementEvent> void collectOutcomes(GameData gameData, E event, Set<E> outcomes,
                                                              int[] leaves) {
        if (leaves[0] >= MAX_PREVIEW_LEAVES) {
            return;
        }
        List<Candidate<E>> candidates = applicable(gameData, event);
        if (candidates.isEmpty()) {
            leaves[0]++;
            outcomes.add(withoutAppliedKeys(event));
            return;
        }
        for (Candidate<E> candidate : candidates) {
            collectOutcomes(gameData, preview(gameData, event, candidate), outcomes, leaves);
        }
    }

    private <E extends ReplacementEvent> boolean orderIsIrrelevant(GameData gameData, E event,
                                                                   List<Candidate<E>> candidates) {
        return allPure(gameData, event, candidates, new int[]{0})
                && possibleOutcomes(gameData, event).size() == 1;
    }

    private <E extends ReplacementEvent> boolean allPure(GameData gameData, E event,
                                                         List<Candidate<E>> candidates, int[] visited) {
        for (Candidate<E> candidate : candidates) {
            if (!candidate.option().pure() || ++visited[0] > MAX_PREVIEW_LEAVES) {
                return false;
            }
            E next = preview(gameData, event, candidate);
            if (!allPure(gameData, next, applicable(gameData, next), visited)) {
                return false;
            }
        }
        return true;
    }

    private <E extends ReplacementEvent> List<Candidate<E>> applicable(GameData gameData, E event) {
        List<Candidate<E>> candidates = new ArrayList<>();
        for (ReplacementCandidateSource<?> source : sources) {
            if (!source.eventType().isInstance(event)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            ReplacementCandidateSource<E> typed = (ReplacementCandidateSource<E>) source;
            for (ReplacementOption option : typed.applicable(gameData, event)) {
                if (!event.appliedKeys().contains(option.key())) {
                    candidates.add(new Candidate<>(typed, option));
                }
            }
        }
        if (candidates.stream().anyMatch(c -> c.option().kind() == ReplacementOption.Kind.SELF_REPLACEMENT)) {
            candidates.removeIf(c -> c.option().kind() != ReplacementOption.Kind.SELF_REPLACEMENT);
        }
        return withUniqueLabels(candidates);
    }

    private static <E extends ReplacementEvent> List<Candidate<E>> withUniqueLabels(List<Candidate<E>> candidates) {
        Map<String, Integer> totals = new HashMap<>();
        candidates.forEach(c -> totals.merge(c.option().label(), 1, Integer::sum));
        Map<String, Integer> seen = new HashMap<>();
        List<Candidate<E>> unique = new ArrayList<>(candidates.size());
        for (Candidate<E> candidate : candidates) {
            ReplacementOption option = candidate.option();
            if (totals.get(option.label()) == 1) {
                unique.add(candidate);
                continue;
            }
            int index = seen.merge(option.label(), 1, Integer::sum);
            unique.add(new Candidate<>(candidate.source(), new ReplacementOption(option.key(),
                    option.label() + " (" + index + ")", option.kind(), option.pure())));
        }
        return unique;
    }

    @SuppressWarnings("unchecked")
    private <E extends ReplacementEvent> ReplacementCandidateSource.ApplyResult<E> applyCandidate(
            GameData gameData, E event, Candidate<E> candidate) {
        String key = candidate.option().key();
        // The effect is spent from the moment it applies (CR 614.5), so damage it redirects carries it.
        return switch (candidate.source().apply(gameData, (E) event.withApplied(key), candidate.option())) {
            case ReplacementCandidateSource.ApplyResult.Applied<E> applied ->
                    new ReplacementCandidateSource.ApplyResult.Applied<>((E) applied.event().withApplied(key));
            case ReplacementCandidateSource.ApplyResult.Suspended<E> suspended ->
                    new ReplacementCandidateSource.ApplyResult.Suspended<>((E) suspended.event().withApplied(key));
        };
    }

    @SuppressWarnings("unchecked")
    private <E extends ReplacementEvent> E preview(GameData gameData, E event, Candidate<E> candidate) {
        E modified = candidate.option().pure()
                ? candidate.source().transform(gameData, event, candidate.option())
                : event;
        return (E) modified.withApplied(candidate.option().key());
    }

    @SuppressWarnings("unchecked")
    private static <E extends ReplacementEvent> E withoutAppliedKeys(E event) {
        return (E) event.withoutAppliedKeys();
    }

    private <E extends ReplacementEvent> void prompt(GameData gameData, E event, List<Candidate<E>> candidates) {
        List<ReplacementOption> options = candidates.stream().map(Candidate::option).toList();
        PendingInteraction.ColorChoice choice = new PendingInteraction.ColorChoice(
                event.chooserId(), null, null, new ChoiceContext.ReplacementEffectOrder(event, options),
                options.stream().map(ReplacementOption::label).toList(), PROMPT);
        if (gameData.interaction.isAwaitingInput()) {
            gameData.queueInteraction(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }
}
