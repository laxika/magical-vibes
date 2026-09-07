package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesExceptChosenTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Harsh Mercy's per-player type choices and non-regenerable creature sweep. */
@Component
@RequiredArgsConstructor
public class DestroyAllCreaturesExceptChosenTypesEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllCreaturesExceptChosenTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = apnapPlayers(gameData);
        UUID nextPlayer = players.stream()
                .filter(playerId -> !entry.getChosenCreatureTypes().containsKey(playerId))
                .findFirst()
                .orElse(null);

        if (nextPlayer == null) {
            destroyUnchosenCreatures(gameData, entry);
            return;
        }

        if (gameData.chosenSpellSubtype != null) {
            entry.getChosenCreatureTypes().put(nextPlayer, gameData.chosenSpellSubtype);
            gameData.chosenSpellSubtype = null;
            nextPlayer = players.stream()
                    .filter(playerId -> !entry.getChosenCreatureTypes().containsKey(playerId))
                    .findFirst()
                    .orElse(null);
            if (nextPlayer == null) {
                destroyUnchosenCreatures(gameData, entry);
                return;
            }
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginSpellCreatureTypeChoice(gameData, nextPlayer);
    }

    private void destroyUnchosenCreatures(GameData gameData, StackEntry entry) {
        gameData.rerunCurrentEffectAfterInteraction = false;
        EnumSet<CardSubtype> chosenTypes = entry.getChosenCreatureTypes().values().stream()
                .collect(() -> EnumSet.noneOf(CardSubtype.class), EnumSet::add, EnumSet::addAll);
        PermanentPredicate filter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(chosenTypes))));

        List<com.github.laxika.magicalvibes.model.Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.forEach(permanent -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                toDestroy.add(permanent);
            }
        }));
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), true);
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
