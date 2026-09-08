package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesOfChosenManaValueParityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link ExileAllCreaturesOfChosenManaValueParityEffect} by asking for odd or even and
 * then exiling all matching creatures on every battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllCreaturesOfChosenManaValueParityEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllCreaturesOfChosenManaValueParityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellManaValueParity == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellManaValueParityChoice(gameData, entry.getControllerId());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        ManaValueParity chosenParity = gameData.chosenSpellManaValueParity;
        gameData.chosenSpellManaValueParity = null;

        List<Permanent> toExile = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                int manaValue = permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue();
                if (gameQueryService.isCreature(gameData, permanent)
                        && chosenParity.matches(manaValue)) {
                    toExile.add(permanent);
                }
            }
        });

        for (Permanent permanent : toExile) {
            permanentRemovalService.removePermanentToExile(gameData, permanent);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}", gameData.id, permanent.getCard().getName(),
                    entry.getCard().getName());
        }

        entry.setEventValue(toExile.size());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
