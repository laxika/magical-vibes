package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MobVerdictEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link MobVerdictEffect}: for each vote of the resolution's VOTE step cast for an opponent of the controller,
 * 2 damage to that player and each creature they control, the opponents in the order their first votes were cast.
 */
@Component
@RequiredArgsConstructor
public class MobVerdictEffectHandler implements NormalEffectHandlerBean {

    private static final int DAMAGE_PER_VOTE = 2;

    private final GameQueryService gameQueryService;
    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MobVerdictEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, Integer> received = new LinkedHashMap<>();
        for (StepOutcome.Vote vote : entry.getResolutionResults().outcome(StepKind.VOTE).votes()) {
            if (vote.candidateId() != null && vote.choice().startsWith(StepOutcome.Vote.PLAYER)
                    && !vote.candidateId().equals(entry.getControllerId())) {
                received.merge(vote.candidateId(), 1, Integer::sum);
            }
        }
        received.forEach((playerId, votes) -> {
            for (int i = 0; i < votes; i++) {
                dealDamageToPlayerAndCreatures(gameData, entry, playerId);
            }
        });
        gameOutcomeService.checkWinCondition(gameData);
    }

    private void dealDamageToPlayerAndCreatures(GameData gameData, StackEntry entry, UUID playerId) {
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, DAMAGE_PER_VOTE);
        List<Permanent> battlefield = new ArrayList<>(gameData.playerBattlefields.getOrDefault(playerId, List.of()));
        damageSupport.damageFilteredCreatures(gameData, entry, DAMAGE_PER_VOTE, battlefield,
                permanent -> gameQueryService.isCreature(gameData, permanent));
    }
}
