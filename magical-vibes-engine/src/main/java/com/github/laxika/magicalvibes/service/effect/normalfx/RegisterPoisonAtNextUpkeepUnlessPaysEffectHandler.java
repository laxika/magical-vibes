package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPoisonAtNextUpkeepUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Sabertooth Cobra's {@code ON_DAMAGE_TO_PLAYER} trigger: reads the damaged player from the
 * stack entry's non-targeting {@code targetId} (recorded by the damage collector) and adds a
 * delayed triggered ability for that player's next upkeep.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterPoisonAtNextUpkeepUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterPoisonAtNextUpkeepUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterPoisonAtNextUpkeepUnlessPaysEffect) effect;
        UUID playerId = entry.getTargetId();
        if (playerId == null) return;

        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStepUnlessPaid(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER),
                        new GivePoisonCountersEffect(e.amount(), Subjects.targetPlayer())),
                entry.getSourcePermanentId(), null, playerId, e.manaCost()));

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " will get another poison counter at the"
                + " beginning of their next upkeep. (", entry.getCard(), ")"));
        log.info("Game {} - {} scheduled an upkeep pay-or-poison obligation on {}",
                gameData.id, entry.getCard().getName(), playerName);
    }
}
