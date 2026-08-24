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
                        || !permanent.getId().equals(entry.getSourcePermanentId()))) {
                    applyEffect(gameData, entry, e, permanent);
                    count[0]++;
                }
            });
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count[0] + " creature(s) lose all abilities until end of turn.").build());
            return;
        }

        UUID targetId = switch (e.scope()) {
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

        applyEffect(gameData, entry, e, target);

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " loses all abilities until end of turn."));
        log.info("Game {} - {} loses all abilities until end of turn", gameData.id, target.getCard().getName());
    }

    private void applyEffect(GameData gameData, StackEntry entry, LosesAllAbilitiesEffect e, Permanent target) {
        if (e.duration() == EffectDuration.PERMANENT) {
            target.setLosesAllAbilitiesPermanently(true);
        } else {
            target.setLosesAllAbilitiesUntilEndOfTurn(true);
        }

        // CR 613 layer engine: a one-shot "loses all abilities until end of turn" (Merfolk
        // Trickster) is a floating layer-6 effect with its own timestamp — a later-timestamp
        // keyword grant (Wings of Velis Vel) survives it. The legacy flag is still set for
        // direct Permanent.hasKeyword/flag readers; the layered pass treats the flag as a
        // seed-time removal and then replays this effect at its real timestamp.
        target.setLosesAllAbilitiesUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(), e,
                target.getId(), null, null,
                e.duration() == EffectDuration.PERMANENT
                        ? EffectDuration.PERMANENT : EffectDuration.UNTIL_END_OF_TURN,
                0));
    }
}
