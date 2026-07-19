package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DamageAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDamageAtNextUpkeepUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Quenchable Fire's SPELL registrar: reads the spell's target (player or planeswalker) from
 * the stack entry's {@code targetId} and queues a {@link DamageAtNextUpkeepUnlessPays} delayed action,
 * drained at the spell controller's next upkeep.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDamageAtNextUpkeepUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDamageAtNextUpkeepUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDamageAtNextUpkeepUnlessPaysEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID spellControllerId = entry.getControllerId();
        gameData.queueDelayedAction(new DamageAtNextUpkeepUnlessPays(
                spellControllerId, targetId, e.damage(), e.manaCost(), entry.getCard()));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName()
                + " will deal an additional " + e.damage() + " damage at the beginning of "
                + gameData.playerIdToName.get(spellControllerId) + "'s next upkeep unless " + e.manaCost()
                + " is paid first."));
        log.info("Game {} - {} scheduled an upkeep pay-or-take-{}-damage obligation for {}",
                gameData.id, entry.getCard().getName(), e.damage(), gameData.playerIdToName.get(spellControllerId));
    }
}
