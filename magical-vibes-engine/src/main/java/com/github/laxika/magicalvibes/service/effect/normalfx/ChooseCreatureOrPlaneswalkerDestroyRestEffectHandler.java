package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOrPlaneswalkerDestroyRestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseCreatureOrPlaneswalkerDestroyRestEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentPredicate CREATURE_OR_PLANESWALKER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsPlaneswalkerPredicate()));

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureOrPlaneswalkerDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> matchingIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                        CREATURE_OR_PLANESWALKER)) {
                    matchingIds.add(permanent.getId());
                }
            }
        });

        if (matchingIds.isEmpty()) {
            destructionSupport.performDestroyAllMatchingExcept(
                    gameData, entry.getCard().getName(), List.of(), CREATURE_OR_PLANESWALKER);
            return;
        }

        destructionSupport.beginNextDestroyRestChoice(
                gameData,
                List.of(new PendingForcedSacrifice(entry.getControllerId(), 1, List.copyOf(matchingIds))),
                List.of(),
                entry.getCard().getName(),
                CREATURE_OR_PLANESWALKER,
                "Choose a creature or planeswalker to keep.",
                true);
    }
}
