package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveKeywordEffect) effect;

        if (remove.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            UUID targetPlayerId = entry.targetsForEffect(effect).stream()
                    .filter(gameData.playerIds::contains)
                    .findFirst()
                    .orElse(entry.getTargetId());
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield == null) {
                return;
            }
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && (remove.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                        permanent, remove.filter(), filterContext))) {
                    removeFrom(gameData, entry, remove, permanent);
                }
            }
            return;
        }

        // Mass one-shot removal floats a per-permanent layer-6 removal. Invert the Skies =
        // opponents; Hour of Devastation = all creatures; Spectacular Pileup = all permanents
        // narrowed to creatures and Vehicles.
        if (remove.scope() == GrantScope.OPPONENT_CREATURES
                || remove.scope() == GrantScope.ALL_CREATURES
                || remove.scope() == GrantScope.ALL_PERMANENTS) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            for (UUID playerId : gameData.playerIds) {
                if (remove.scope() == GrantScope.OPPONENT_CREATURES
                        && playerId.equals(entry.getControllerId())) {
                    continue;
                }
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    if (remove.scope() != GrantScope.ALL_PERMANENTS
                            && !gameQueryService.isCreature(gameData, p)) {
                        continue;
                    }
                    if (remove.scope() == GrantScope.ALL_PERMANENTS
                            && entry.getSourcePermanentId() != null
                            && entry.getSourcePermanentId().equals(p.getId())) {
                        continue;
                    }
                    if (remove.filter() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(
                                    p, remove.filter(), filterContext)) {
                        continue;
                    }
                    removeFrom(gameData, entry, remove, p);
                }
            }
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (remove.scope() == GrantScope.TARGET && !targetIds.isEmpty()) {
            for (UUID multiTargetId : targetIds) {
                Permanent multiTarget = gameQueryService.findPermanentById(gameData, multiTargetId);
                if (multiTarget != null) {
                    removeFrom(gameData, entry, remove, multiTarget);
                }
            }
            return;
        }

        UUID targetId = switch (remove.scope()) {
            case SELF -> entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            case TARGET -> entry.getTargetId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }
        removeFrom(gameData, entry, remove, target);
    }

    private void removeFrom(GameData gameData, StackEntry entry, RemoveKeywordEffect remove, Permanent target) {
        // CR 613 layer engine: a one-shot keyword removal is a floating layer-6 effect with
        // its own timestamp — a later-timestamp grant of the same keyword re-adds it. The
        // legacy field is still written for direct Permanent.hasKeyword callers; the layered
        // pass seeds it and then replays this removal at its real timestamp.
        target.getRemovedKeywords().add(remove.keyword());
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(), remove,
                target.getId(), null, null, remove.duration(), 0));
        String keywordName = remove.keyword().name().charAt(0) + remove.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String durationLabel = remove.duration() == EffectDuration.UNTIL_END_OF_TURN ? " until end of turn" : "";
        
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" loses " + keywordName + durationLabel + ".").build());
        log.info("Game {} - {} loses {} ({})", gameData.id, target.getCard().getName(), remove.keyword(), remove.scope());
    }
}
