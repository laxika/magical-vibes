package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerWithMostCreaturesGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                    source = permanent;
                    break;
                }
            }
            if (source != null) {
                break;
            }
        }

        if (source == null) {
            return;
        }

        PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect mostCreaturesEffect =
                (PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect) effect;
        PermanentPredicate creatureFilter = mostCreaturesEffect.creatureFilter();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int highestCreatureCount = -1;
        UUID playerWithMostCreatures = null;
        boolean tiedForMostCreatures = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            int creatureCount = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            permanent, creatureFilter, filterContext)) {
                        creatureCount++;
                    }
                }
            }
            if (creatureCount > highestCreatureCount) {
                highestCreatureCount = creatureCount;
                playerWithMostCreatures = playerId;
                tiedForMostCreatures = false;
            } else if (creatureCount == highestCreatureCount) {
                tiedForMostCreatures = true;
            }
        }

        if (tiedForMostCreatures || playerWithMostCreatures == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, playerWithMostCreatures, source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
