package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantKeywordEffect) effect;
        if (grant.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (grant.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    continue;
                }
                bucketFor(permanent, grant.duration()).addAll(grant.keywords());
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            String logEntry = entry.getCard().getName() + " gives " + keywordNames + " to " + count + " creature(s) " + durationLabel(grant.duration()) + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} own creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (!gameQueryService.isCreature(gameData, permanent)) {
                        continue;
                    }
                    if (grant.filter() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                        continue;
                    }
                    bucketFor(permanent, grant.duration()).addAll(grant.keywords());
                    count++;
                }
            }

            String keywordNames = formatKeywords(grant.keywords());
            String logEntry = entry.getCard().getName() + " gives " + keywordNames + " to " + count + " creature(s) " + durationLabel(grant.duration()) + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} creature(s) target player controls", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.ALL_CREATURES) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            final int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    return;
                }
                if (grant.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    return;
                }
                bucketFor(permanent, grant.duration()).addAll(grant.keywords());
                count[0]++;
            });

            String keywordNames = formatKeywords(grant.keywords());
            String logEntry = entry.getCard().getName() + " gives " + keywordNames + " to " + count[0] + " creature(s) " + durationLabel(grant.duration()) + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count[0]);
            return;
        }

        // SELF resolves against the source; TARGET may cover multiple targets when the effect is
        // bound to a target group (e.g. Blades of Velis Vel: "up to two target creatures").
        List<UUID> ids;
        if (grant.scope() == GrantScope.SELF) {
            UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            ids = selfId != null ? List.of(selfId) : List.of();
        } else if (grant.scope() == GrantScope.TARGET) {
            ids = entry.targetsForEffect(effect);
            if (ids.isEmpty() && entry.getTargetId() != null) {
                ids = List.of(entry.getTargetId());
            }
        } else if (grant.scope() == GrantScope.TOKENS_CREATED_THIS_RESOLUTION) {
            ids = List.copyOf(entry.getCreatedPermanentIds());
        } else {
            return;
        }

        for (UUID id : ids) {
            Permanent target = gameQueryService.findPermanentById(gameData, id);
            if (target == null) {
                continue; // Partially resolves — skip removed targets
            }

            // Optional grant condition: the target stays legal either way; only the keyword grant
            // is conditional (e.g. Vampire's Zeal grants first strike only if the target is a Vampire).
            if (grant.grantCondition() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, target, grant.grantCondition())) {
                continue;
            }

            bucketFor(target, grant.duration()).addAll(grant.keywords());
            String keywordNames = formatKeywords(grant.keywords());
            String logEntry = target.getCard().getName() + " gains " + keywordNames + " " + durationLabel(grant.duration()) + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keywords(), grant.scope());
        }
    }

    private Set<Keyword> bucketFor(Permanent permanent, GrantDuration duration) {
        return duration == GrantDuration.UNTIL_YOUR_NEXT_TURN
                ? permanent.getUntilNextTurnKeywords()
                : permanent.getGrantedKeywords();
    }

    private String durationLabel(GrantDuration duration) {
        return duration == GrantDuration.UNTIL_YOUR_NEXT_TURN
                ? "until your next turn"
                : "until end of turn";
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(k -> k.name().charAt(0) + k.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
