package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LosesAllAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LosesAllAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LosesAllAbilitiesEffect) effect;
        if (e.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)) {
                        applyEffect(gameData, entry, e, permanent);
                        count++;
                    }
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) lose all abilities until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            int count = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)) {
                        applyEffect(gameData, entry, e, permanent);
                        count++;
                    }
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) lose all abilities until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.ALL_CREATURES
                || e.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF) {
            final int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                if (gameQueryService.isCreature(gameData, permanent)
                        && (e.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF
                        || entry.getSourcePermanentId() == null
                        || !permanent.getId().equals(entry.getSourcePermanentId()))
                        && applyEffect(gameData, entry, e, permanent)) {
                    count[0]++;
                }
            });
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count[0] + " creature(s) lose all abilities until end of turn.").build());
            return;
        }

        List<UUID> targetIds;
        if (e.scope() == GrantScope.SELF) {
            UUID sourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            targetIds = sourceId == null ? List.of() : List.of(sourceId);
        } else if (e.scope() == GrantScope.TARGET) {
            targetIds = entry.targetsForEffect(effect);
            if (targetIds.isEmpty() && entry.getTargetId() != null) {
                targetIds = List.of(entry.getTargetId());
            }
        } else {
            return;
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            if (!applyEffect(gameData, entry, e, target)) {
                continue;
            }

            String durationText = switch (e.duration()) {
                case CONTINUOUS, PERMANENT -> "indefinitely";
                case WHILE_SOURCE_ON_BATTLEFIELD, WHILE_SOURCE_REMAINS,
                        WHILE_SOURCE_TAPPED, WHILE_SOURCE_REMAINS_TAPPED, WHILE_ATTACHED ->
                        "for as long as its source remains on the battlefield";
                default -> "until end of turn";
            };
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " loses all abilities " + durationText + "."));
            log.info("Game {} - {} loses all abilities {}", gameData.id, target.getCard().getName(), durationText);
        }
    }

    private boolean applyEffect(GameData gameData, StackEntry entry, LosesAllAbilitiesEffect e, Permanent target) {
        if (e.duration() == EffectDuration.PERMANENT) {
            target.setLosesAllAbilitiesPermanently(true);
        } else if (e.duration() == EffectDuration.UNTIL_END_OF_TURN) {
            target.setLosesAllAbilitiesUntilEndOfTurn(true);
        }

        // CR 613 layer engine: a one-shot "loses all abilities until end of turn" (Merfolk
        // Trickster) is a floating layer-6 effect with its own timestamp — a later-timestamp
        // keyword grant (Wings of Velis Vel) survives it. The legacy flag is still set for
        // direct Permanent.hasKeyword/flag readers; the layered pass treats the flag as a
        // seed-time removal and then replays this effect at its real timestamp.
        boolean sourceLinked = e.duration() == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD
                || e.duration() == EffectDuration.WHILE_SOURCE_REMAINS
                || e.duration() == EffectDuration.WHILE_SOURCE_TAPPED
                || e.duration() == EffectDuration.WHILE_SOURCE_REMAINS_TAPPED
                || e.duration() == EffectDuration.WHILE_ATTACHED;
        UUID sourcePermanentId = sourceLinked ? entry.getSourcePermanentId() : null;
        if (sourceLinked) {
            if (sourcePermanentId == null
                    || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                return false;
            }
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), sourcePermanentId, entry.getControllerId(), e,
                target.getId(), null, null,
                e.duration() == EffectDuration.CONTINUOUS
                        ? EffectDuration.PERMANENT : e.duration(),
                0));
        return true;
    }
}
