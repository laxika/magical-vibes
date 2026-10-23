package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerChoosesUpToPermanentsThenSacrificesRestEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a player's choice of permanents to keep before sacrificing the rest. */
@Component
@RequiredArgsConstructor
public class PlayerChoosesUpToPermanentsThenSacrificesRestEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerChoosesUpToPermanentsThenSacrificesRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PlayerChoosesUpToPermanentsThenSacrificesRestEffect choiceEffect =
                (PlayerChoosesUpToPermanentsThenSacrificesRestEffect) effect;
        UUID playerId = entry.getTargetId();
        if (playerId == null) {
            return;
        }

        List<UUID> candidateIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, choiceEffect.filter())) {
                    candidateIds.add(permanent.getId());
                }
            }
        }

        if (candidateIds.isEmpty()) {
            return;
        }

        List<UUID> choosableIds = candidateIds.stream()
                .filter(id -> !gameQueryService.cantBeChosen(gameData, gameQueryService.findPermanentById(gameData, id)))
                .toList();
        if (choosableIds.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, candidateIds);
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                playerId,
                choosableIds,
                Math.min(choiceEffect.maxCount(), choosableIds.size()),
                new MultiPermanentChoiceContext.PlayerChoosesUpToPermanentsThenSacrificesRestChoice(
                        List.copyOf(candidateIds)),
                "Choose up to " + choiceEffect.maxCount() + " permanents to keep.");
    }

    public void completeChoice(GameData gameData, List<UUID> keptIds,
                               MultiPermanentChoiceContext.PlayerChoosesUpToPermanentsThenSacrificesRestChoice context) {
        Set<UUID> kept = new HashSet<>(keptIds);
        List<UUID> toSacrifice = context.candidateIds().stream()
                .filter(permanentId -> !kept.contains(permanentId))
                .toList();
        destructionSupport.performSimultaneousSacrifice(gameData, toSacrifice);
    }
}
