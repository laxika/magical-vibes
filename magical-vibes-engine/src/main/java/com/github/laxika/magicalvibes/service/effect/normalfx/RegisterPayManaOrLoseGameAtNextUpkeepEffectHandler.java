package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterPayManaOrLoseGameAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the Pact of Negation delayed payment registrar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterPayManaOrLoseGameAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterPayManaOrLoseGameAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registrar = (RegisterPayManaOrLoseGameAtNextUpkeepEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.upkeep(ActivePlayer.YOU), entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), null,
                new ForcedCostOrElseEffect(new PayManaCost(registrar.manaCost()),
                        List.of(new ControllerLosesGameEffect()), true)));

        gameLogService.append(gameData, GameLog.text(entry.getCard().getName()
                + " requires its controller to pay " + registrar.manaCost()
                + " at their next upkeep or lose the game."));
        log.info("Game {} - {} scheduled an upkeep pay-or-lose-game obligation for {}",
                gameData.id, entry.getCard().getName(), gameData.playerIdToName.get(entry.getControllerId()));
    }
}
