package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToTargetAndAddToughnessCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.PutCountersForPreventedDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventNextDamageToTargetAndAddToughnessCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageToTargetAndAddToughnessCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var prevent = (PreventNextDamageToTargetAndAddToughnessCountersEffect) effect;
        UUID targetId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamageToCounterPreventionShield(
                    target.getDamageToCounterPreventionShield() + prevent.amount());
            boolean creature = gameQueryService.isCreature(gameData, target);
            UUID shieldId = gameData.registerSacredBoonShield(entry.getCard().getName(), entry.getControllerId(),
                    target.getId(), prevent.amount(), creature);
            if (creature) {
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                        Triggers.endStep(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                        entry.getSourcePermanentId(), target.getId(),
                        new PutCountersForPreventedDamageEffect(shieldId)));
            }
            gameLogService.append(gameData, GameLog.builder()
                    .text("The next " + prevent.amount() + " damage that would be dealt to ")
                    .card(target.getCard())
                    .text(" is prevented.")
                    .build());
            log.info("Game {} - Prevention-to-counters shield {} added to permanent {}",
                    gameData.id, prevent.amount(), target.getCard().getName());
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            gameData.addPlayerDamagePreventionShield(entry.getCard().getName(), entry.getControllerId(), targetId,
                    prevent.amount(), false);
            String playerName = gameData.playerIdToName.get(targetId);
            gameLogService.append(gameData, GameLog.text(
                    "The next " + prevent.amount() + " damage that would be dealt to "
                            + playerName + " is prevented."));
            log.info("Game {} - Prevention shield {} added to player {}",
                    gameData.id, prevent.amount(), playerName);
        }
    }
}
