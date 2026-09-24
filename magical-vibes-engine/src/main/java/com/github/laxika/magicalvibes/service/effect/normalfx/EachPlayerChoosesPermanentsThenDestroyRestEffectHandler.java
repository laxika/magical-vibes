package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPermanentsThenDestroyRestEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerChoosesPermanentsThenDestroyRestEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesPermanentsThenDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerChoosesPermanentsThenDestroyRestEffect choiceEffect =
                (EachPlayerChoosesPermanentsThenDestroyRestEffect) effect;
        List<UUID> protectedIds = new ArrayList<>();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();

        for (UUID playerId : apnapPlayers(gameData)) {
            List<Permanent> choices = matchingPermanents(gameData, playerId, choiceEffect);
            if (choices.size() <= choiceEffect.maxCount()) {
                protectedIds.addAll(choices.stream().map(Permanent::getId).toList());
            } else if (choiceEffect.maxCount() > 0) {
                choosers.add(new PendingForcedSacrifice(
                        playerId,
                        choiceEffect.maxCount(),
                        choices.stream().map(Permanent::getId).toList()));
            }
        }

        if (choosers.isEmpty()) {
            destructionSupport.performDestroyAllMatchingExcept(
                    gameData, entry.getCard().getName(), protectedIds, choiceEffect.filter());
            return;
        }

        destructionSupport.beginNextDestroyRestChoice(
                gameData,
                choosers,
                protectedIds,
                entry.getCard().getName(),
                choiceEffect.filter(),
                "Choose " + choiceEffect.maxCount() + " permanents to keep.",
                choiceEffect.maxCount());
    }

    private List<Permanent> matchingPermanents(GameData gameData, UUID playerId,
                                                EachPlayerChoosesPermanentsThenDestroyRestEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, effect.filter()))
                .toList();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
            rotated.addAll(players.subList(0, activeIndex));
            return rotated;
        }
        return players;
    }
}
