package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MayPayManaAndSacrificePermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayManaAndSacrificePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayManaAndSacrificePermanentEffect e = (MayPayManaAndSacrificePermanentEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = matchingPermanentIds(gameData, entry, e);
        if (matchingIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no "
                            + e.permanentDescription() + " to sacrifice."));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(e),
                entry.getCard().getName() + " - Pay " + e.manaCost()
                        + " and sacrifice " + e.permanentDescription() + "?",
                entry.getTargetId(),
                e.manaCost(),
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                entry.getAttackedTargetId(),
                entry.getActivePlayerId(),
                null,
                entry.getSourcePermanentSnapshot(),
                entry.getControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue()
        ));
    }

    private List<UUID> matchingPermanentIds(
            GameData gameData, StackEntry entry, MayPayManaAndSacrificePermanentEffect effect) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> matchingIds = new ArrayList<>();
        var battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield != null) {
            for (var permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, effect.filter(), filterContext)) {
                    matchingIds.add(permanent.getId());
                }
            }
        }
        return matchingIds;
    }
}
