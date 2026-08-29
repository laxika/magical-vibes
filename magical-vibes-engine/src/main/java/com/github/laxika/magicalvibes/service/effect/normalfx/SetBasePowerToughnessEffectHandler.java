package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
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
public class SetBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessEffect) effect;
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
                    .text(" sets the base power and toughness of " + count + " creature(s) to "
                            + e.power() + "/" + e.toughness() + " until end of turn.").build());
            return;
        }

        if (e.scope() == GrantScope.TARGET) {
            List<UUID> targetIds = entry.targetsForEffect(e);
            if (targetIds.isEmpty() && entry.getTargetId() != null) {
                targetIds = List.of(entry.getTargetId());
            }
            for (UUID targetId : targetIds) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                applyEffect(gameData, entry, e, target);
                String description = basePowerToughnessDescription(e);
                gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(description).build());
                log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), description);
            }
            return;
        }

        if (e.scope() == GrantScope.ALL_CREATURES
                || e.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            SetBasePowerToughnessEffect individualEffect = new SetBasePowerToughnessEffect(
                    e.power(), e.toughness(), GrantScope.TARGET, e.duration());
            int count = 0;
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (!gameQueryService.isCreature(gameData, permanent)
                            || (e.scope() == GrantScope.ALL_CREATURES
                            && permanent.getId().equals(sourcePermanentId))) {
                        continue;
                    }
                    applyEffect(gameData, entry, individualEffect, permanent);
                    count++;
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" sets the base power and toughness of " + count
                            + " other creature(s) to " + e.power() + "/" + e.toughness()
                            + " until end of turn.").build());
            return;
        }

        // SELF scope ("this creature has base P/T X/Y until end of turn", e.g. Marsh Flitter)
        // resolves against the source.
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (target == null) {
            return;
        }

        applyEffect(gameData, entry, e, target);
        String description = basePowerToughnessDescription(e);
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(description).build());
        log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), description);
    }

    private void applyEffect(GameData gameData, StackEntry entry, SetBasePowerToughnessEffect e, Permanent target) {
        // CR 613 layer engine: a one-shot base-P/T setter is a floating layer-7b effect with
        // its own timestamp — of all applicable 7b setters (auras, animations, other one-shots)
        // the latest timestamp wins in the layered pass. The legacy fields are still written
        // for direct Permanent readers (views, last-known-information); the floating instance
        // is what drives precedence.
        // The legacy UEOT fields are an all-or-nothing pair, so a partial setter
        // ("has base toughness 1") skips them entirely and rides on the floating 7b entry alone,
        // which carries per-component nulls.
        if (e.duration() == EffectDuration.UNTIL_END_OF_TURN
                && e.power() != null && e.toughness() != null) {
            target.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
            target.setBasePowerOverride(e.power());
            target.setBaseToughnessOverride(e.toughness());
        }
        FloatingContinuousEffect floating = gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                e, target.getId(), null, null, e.duration(), 0));
        if (e.duration() == EffectDuration.PERMANENT) {
            long timestamp = floating.timestamp();
            if (e.power() != null) {
                target.setBasePowerOverriddenPermanently(true);
                target.setPermanentBasePowerOverride(e.power());
                target.setPermanentBasePowerOverrideTimestamp(timestamp);
            }
            if (e.toughness() != null) {
                target.setBaseToughnessOverriddenPermanently(true);
                target.setPermanentBaseToughnessOverride(e.toughness());
                target.setPermanentBaseToughnessOverrideTimestamp(timestamp);
            }
        }
    }

    private String basePowerToughnessDescription(SetBasePowerToughnessEffect effect) {
        String duration = effect.duration() == EffectDuration.UNTIL_END_OF_TURN
                ? " until end of turn" : "";
        return effect.power() == null
                ? " has base toughness " + effect.toughness() + duration + "."
                : effect.toughness() == null
                ? " has base power " + effect.power() + duration + "."
                : " has base power and toughness " + effect.power() + "/" + effect.toughness()
                + duration + ".";
    }
}
