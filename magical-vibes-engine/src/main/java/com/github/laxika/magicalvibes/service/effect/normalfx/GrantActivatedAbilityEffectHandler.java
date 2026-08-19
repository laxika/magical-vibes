package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
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
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantActivatedAbilityEffect) effect;
        int count = 0;
        if (e.scope() == GrantScope.TARGET) {
            // "Target creature gains '[ability]' until end of turn" (e.g. Banishing Knack).
            // Bound to a target group; falls back to the single-target id.
            List<UUID> ids = entry.targetsForEffect(effect);
            if (ids.isEmpty() && entry.getTargetId() != null) {
                ids = List.of(entry.getTargetId());
            }
            for (UUID id : ids) {
                Permanent target = gameQueryService.findPermanentById(gameData, id);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                grantTo(gameData, entry, target, e);
                count++;
            }
        } else if (e.scope() == GrantScope.ENCHANTED_PERMANENT) {
            // The Genju cycle: the permanent the source Aura is attached to, re-derived at
            // resolution and without targeting. No creature check — the same ability animates it.
            Permanent aura = entry.getSourcePermanentId() == null ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            Permanent enchanted = aura == null || aura.getAttachedTo() == null ? null
                    : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (enchanted != null) {
                grantTo(gameData, entry, enchanted, e);
                count++;
            }
        } else {
            boolean grantsToAllCreatures = e.scope() == GrantScope.ALL_CREATURES
                    || e.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF;
            List<Permanent> battlefield = grantsToAllCreatures
                    ? gameData.playerBattlefields.values().stream().flatMap(List::stream).toList()
                    : gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            // OWN_CREATURES means "other creatures you control" — the source is excluded.
            // ALL_OWN_CREATURES includes the source.
            boolean excludeSource = e.scope() == GrantScope.OWN_CREATURES
                    || e.scope() == GrantScope.ALL_CREATURES;
            boolean grantsToLands = e.scope() == GrantScope.OWN_LANDS;
            boolean grantsToAllPermanents = e.scope() == GrantScope.OWN_PERMANENTS;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (grantsToLands && !gameQueryService.isLand(gameData, permanent)) {
                        continue;
                    }
                    if (!grantsToLands && !grantsToAllPermanents
                            && !gameQueryService.isCreature(gameData, permanent)) {
                        continue;
                    }
                    if (excludeSource && permanent.getId().equals(entry.getSourcePermanentId())) {
                        continue;
                    }
                    if (e.filter() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                        continue;
                    }
                    grantTo(gameData, entry, permanent, e);
                    count++;
                }
            }
        }

        String durationText = switch (e.duration()) {
            case UNTIL_YOUR_NEXT_TURN -> "until your next turn";
            case WHILE_SOURCE_ON_BATTLEFIELD -> "for as long as the source remains on the battlefield";
            case PERMANENT, CONTINUOUS -> "indefinitely";
            default -> "until end of turn";
        };
        String recipientText = e.scope() == GrantScope.OWN_LANDS ? "land(s)" :
                e.scope() == GrantScope.OWN_PERMANENTS ? "permanent(s)" : "creature(s)";
        
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" grants \"" + e.ability().getDescription() + "\" to " + count + " " + recipientText + " " + durationText + ".").build());
        log.info("Game {} - {} grants activated ability to {} {} {}",
                gameData.id, entry.getCard().getName(), count, recipientText, durationText);
    }

    private static void grantTo(GameData gameData, StackEntry entry, Permanent permanent,
                                GrantActivatedAbilityEffect grant) {
        EffectDuration duration = grant.duration();
        if (duration == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantActivatedAbilityEffect(
                            grant.ability().withGrantSource(entry.getSourcePermanentId()),
                            GrantScope.TARGET,
                            grant.filter(),
                            duration
                    ),
                    permanent.getId(),
                    null,
                    null,
                    duration,
                    0
            ));
            return;
        }
        if (duration == EffectDuration.UNTIL_YOUR_NEXT_TURN) {
            permanent.getUntilNextTurnActivatedAbilities().add(grant.ability());
        } else {
            permanent.getTemporaryActivatedAbilities().add(grant.ability());
        }
    }
}
