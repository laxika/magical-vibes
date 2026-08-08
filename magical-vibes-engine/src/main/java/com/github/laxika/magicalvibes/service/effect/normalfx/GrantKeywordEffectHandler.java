package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;

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
                    .withSourceControllerId(entry.getControllerId())
                    .withXValue(entry.getXValue());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (grant.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    continue;
                }
                // Layer-6 floating effect as well as the legacy bucket, so a grant resolving after
                // an "all creatures you control lose all abilities" on the same entry survives it
                // (CR 613.7 timestamp order) — Dragonshift overloaded.
                bucketFor(permanent, grant.duration()).addAll(grant.keywords());
                gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(), grant,
                        permanent.getId(), null, null, floatingDurationFor(grant.duration()), 0));
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());

            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count + " creature(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} own creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.OWN_PERMANENTS) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (grant.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    continue;
                }
                bucketFor(permanent, grant.duration()).addAll(grant.keywords());
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count + " permanent(s) " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} grants {} to {} own permanent(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count);
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
            
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count + " creature(s) " + durationLabel(grant.duration()) + ".").build());
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
            
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" gives " + keywordNames + " to " + count[0] + " creature(s) " + durationLabel(grant.duration()) + ".").build());
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
        } else if (grant.scope() == GrantScope.BANDED_WITH_SELF) {
            ids = bandmatesOf(gameData, entry.getSourcePermanentId());
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

            // CR 613 layer engine: one-shot keyword grants to explicit permanents are floating
            // layer-6 effects with their own timestamp — a grant resolving after a "loses all
            // abilities" or keyword removal survives it (and vice versa). The legacy bucket is
            // still written for direct Permanent.hasKeyword callers; the layered pass seeds it
            // and then replays this grant at its real timestamp.
            // WHILE_SOURCE_ON_BATTLEFIELD grants live only as floating effects: the legacy
            // buckets are both cleared by turn cleanup, which would silently end the grant.
            if (grant.duration() != GrantDuration.WHILE_SOURCE_ON_BATTLEFIELD) {
                bucketFor(target, grant.duration()).addAll(grant.keywords());
            }
            UUID floatingSourceId = grant.duration() == GrantDuration.WHILE_SOURCE_ON_BATTLEFIELD
                    ? entry.getSourcePermanentId()
                    : null;
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    entry.getCard().getName(), floatingSourceId, entry.getControllerId(), grant,
                    target.getId(), null, null, floatingDurationFor(grant.duration()), 0));
            String keywordNames = formatKeywords(grant.keywords());
            
            gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" gains " + keywordNames + " " + durationLabel(grant.duration()) + ".").build());
            log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keywords(), grant.scope());
        }
    }

    /**
     * The other attacking creatures sharing the source's attacking band (CR 702.22c). The source
     * itself is excluded, and an empty list is returned when it is not attacking in a band.
     */
    private List<UUID> bandmatesOf(GameData gameData, UUID sourceId) {
        if (sourceId == null) {
            return List.of();
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null || source.getBandId() == null) {
            return List.of();
        }
        List<UUID> bandmates = new java.util.ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttacking()
                    && source.getBandId().equals(permanent.getBandId())
                    && !permanent.getId().equals(sourceId)) {
                bandmates.add(permanent.getId());
            }
        });
        return bandmates;
    }

    private EffectDuration floatingDurationFor(GrantDuration duration) {
        return switch (duration) {
            case UNTIL_YOUR_NEXT_TURN -> EffectDuration.UNTIL_YOUR_NEXT_TURN;
            case WHILE_SOURCE_ON_BATTLEFIELD -> EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD;
            case INDEFINITE -> EffectDuration.PERMANENT;
            case END_OF_TURN -> EffectDuration.UNTIL_END_OF_TURN;
        };
    }

    private Set<Keyword> bucketFor(Permanent permanent, GrantDuration duration) {
        return switch (duration) {
            case UNTIL_YOUR_NEXT_TURN -> permanent.getUntilNextTurnKeywords();
            case INDEFINITE -> permanent.getPersistentGrantedKeywords();
            default -> permanent.getGrantedKeywords();
        };
    }

    private String durationLabel(GrantDuration duration) {
        return switch (duration) {
            case UNTIL_YOUR_NEXT_TURN -> "until your next turn";
            case WHILE_SOURCE_ON_BATTLEFIELD -> "for as long as you control its source";
            case INDEFINITE -> "indefinitely";
            case END_OF_TURN -> "until end of turn";
        };
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(k -> k.name().charAt(0) + k.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
