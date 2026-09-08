package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureForEachPlayerDestroyRestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseCreatureForEachPlayerDestroyRestEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreatureForEachPlayerDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseCreatureForEachPlayerDestroyRestEffect choiceEffect =
                (ChooseCreatureForEachPlayerDestroyRestEffect) effect;
        List<UUID> protectedIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> choices = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, choiceEffect.choiceFilter())) {
                        choices.add(permanent);
                    }
                }
            }

            if (choices.size() == 1) {
                protectedIds.add(choices.getFirst().getId());
            } else if (choices.size() > 1) {
                choosers.add(new PendingForcedSacrifice(entry.getControllerId(), 1,
                        choices.stream().map(Permanent::getId).toList()));
            }
        }

        if (choosers.isEmpty()) {
            destructionSupport.performDestroyAllCreaturesExcept(
                    gameData, entry.getCard().getName(), protectedIds);
        } else {
            destructionSupport.beginNextDestroyRestChoice(
                    gameData, choosers, protectedIds, entry.getCard().getName(),
                    new PermanentIsCreaturePredicate(), "Choose a creature to keep.", false);
        }
    }
}
