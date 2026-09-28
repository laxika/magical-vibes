package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TurfWarCombatDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Turf War's contested-land control change. */
@Component
@RequiredArgsConstructor
public class TurfWarCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final CreatureControlService creatureControlService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurfWarCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TurfWarCombatDamageEffect turfWarEffect = (TurfWarCombatDamageEffect) effect;
        UUID damagedPlayerId = turfWarEffect.damagedPlayerIsController()
                ? entry.getControllerId() : entry.getTargetId();
        chooseOrApply(gameData, damagedPlayerId, entry.getTriggeringPermanentControllerId(),
                entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.TurfWarLandChoice context) {
        if (chosenIds.isEmpty()) {
            return;
        }
        Permanent land = gameQueryService.findPermanentById(gameData, chosenIds.getFirst());
        if (land != null && isContestedLandControlledBy(gameData, land, context.damagedPlayerId())) {
            applyControlAndUntap(gameData, land, context.creatureControllerId(), context.sourceName());
        }
    }

    private void chooseOrApply(GameData gameData, UUID damagedPlayerId, UUID creatureControllerId,
                               String sourceName) {
        if (damagedPlayerId == null || creatureControllerId == null) {
            return;
        }
        List<UUID> candidates = contestedLandIds(gameData, damagedPlayerId);
        if (candidates.isEmpty()) {
            return;
        }
        if (candidates.size() == 1) {
            Permanent land = gameQueryService.findPermanentById(gameData, candidates.getFirst());
            if (land != null) {
                applyControlAndUntap(gameData, land, creatureControllerId, sourceName);
            }
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData, creatureControllerId, candidates, 1,
                new MultiPermanentChoiceContext.TurfWarLandChoice(
                        creatureControllerId, damagedPlayerId, sourceName),
                sourceName + " — choose a contested land to gain control of.");
    }

    private void applyControlAndUntap(GameData gameData, Permanent land, UUID newControllerId,
                                      String sourceName) {
        boolean controlApplied = creatureControlService.applyControlEffect(
                gameData, newControllerId, land,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT, null, sourceName);
        if (controlApplied) {
            tapUntapSupport.untapPermanent(gameData, land);
        }
    }

    private List<UUID> contestedLandIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> isContestedLandControlledBy(gameData, permanent, playerId))
                .map(Permanent::getId)
                .toList();
    }

    private boolean isContestedLandControlledBy(GameData gameData, Permanent permanent, UUID playerId) {
        return playerId.equals(gameData.findControllerOf(permanent))
                && gameQueryService.isLand(gameData, permanent)
                && permanent.getCounterCount(CounterType.CONTESTED) > 0;
    }
}
