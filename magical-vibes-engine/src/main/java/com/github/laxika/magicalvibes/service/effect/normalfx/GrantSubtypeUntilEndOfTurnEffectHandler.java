package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantSubtypeUntilEndOfTurnEffect) effect;
        FilterContext filterContext = new FilterContext(
                gameData,
                entry.getCard() != null ? entry.getCard().getId() : null,
                entry.getControllerId(),
                null,
                entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()),
                entry.getSourcePermanentId());
        if (e.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)
                            && matchesFilter(permanent, e, filterContext)) {
                        applyEffect(gameData, entry, e, permanent);
                        count++;
                    }
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" grants " + e.subtype().getDisplayName() + " to " + count
                            + " creature(s) until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)
                            && matchesFilter(permanent, e, filterContext)) {
                        applyEffect(gameData, entry, e, permanent);
                        count++;
                    }
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" grants " + e.subtype().getDisplayName() + " to " + count
                            + " creature(s) until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.SELF) {
            UUID selfId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            Permanent self = gameQueryService.findPermanentById(gameData, selfId);
            if (self == null) {
                return;
            }
            applyEffect(gameData, entry, e, self);
            gameLogService.append(gameData, GameLog.builder().card(self.getCard())
                    .text(" becomes a " + e.subtype().getDisplayName()
                            + " in addition to its other types until end of turn.").build());
            return;
        }

        for (UUID targetId : entry.targetsForEffect(e)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            applyEffect(gameData, entry, e, target);
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" becomes a " + e.subtype().getDisplayName()
                            + " in addition to its other types until end of turn.").build());
        }
    }

    private void applyEffect(GameData gameData, StackEntry entry,
                             GrantSubtypeUntilEndOfTurnEffect effect, Permanent target) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), new GrantSubtypeEffect(effect.subtype(), GrantScope.TARGET),
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
    }

    private boolean matchesFilter(Permanent permanent, GrantSubtypeUntilEndOfTurnEffect effect,
                                  FilterContext filterContext) {
        return effect.filter() == null
                || predicateEvaluationService.matchesPermanentPredicate(
                permanent, effect.filter(), filterContext);
    }
}
