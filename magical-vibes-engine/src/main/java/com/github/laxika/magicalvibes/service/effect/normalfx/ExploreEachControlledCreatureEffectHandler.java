package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEachControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExploreEachControlledCreatureEffect}. When several permanents are instructed to explore
 * at the same time, their controller chooses one of them, it explores, and the process repeats for the
 * rest (CR 701.44d).
 */
@Component
public class ExploreEachControlledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    public ExploreEachControlledCreatureEffectHandler(PredicateEvaluationService predicateEvaluationService,
                                                      PlayerInputService playerInputService) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.playerInputService = playerInputService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEachControlledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExploreEachControlledCreatureEffect exploreEach = (ExploreEachControlledCreatureEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(
                entry.getControllerId(), List.of());
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        List<UUID> exploringIds = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, exploreEach.predicate(), context)) {
                exploringIds.add(permanent.getId());
            }
        }

        if (exploringIds.size() == 1) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(ExploreEffect.forPermanent(exploringIds.getFirst())));
        } else if (exploringIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExploreOrderChoice(entry, List.copyOf(exploringIds)));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), exploringIds,
                    "Choose which creature explores next.");
        }
    }

    /** Explores the chosen permanent first, then repeats the choice for the rest. */
    public void completeChoice(GameData gameData, UUID chosenId,
                               PermanentChoiceContext.ExploreOrderChoice choice) {
        if (!choice.candidateIds().contains(chosenId)) {
            throw new IllegalStateException("Invalid creature to explore");
        }
        StackEntry entry = choice.resolvingEntry();
        List<UUID> rest = choice.candidateIds().stream().filter(id -> !id.equals(chosenId)).toList();
        List<CardEffect> steps = new ArrayList<>();
        steps.add(ExploreEffect.forPermanent(chosenId));
        if (!rest.isEmpty()) {
            PermanentPredicate remaining = rest.size() == 1
                    ? new PermanentIsSpecificPermanentPredicate(rest.getFirst())
                    : new PermanentAnyOfPredicate(rest.stream()
                    .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
            steps.add(new ExploreEachControlledCreatureEffect(remaining));
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, steps);
    }
}
