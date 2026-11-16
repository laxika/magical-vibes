package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Asks the controller for a planeswalker type, then creates the delayed "until end of turn, whenever you activate an
 * ability of a planeswalker of that type, copy that ability" ability with that type (CR 603.7a), whose source is the
 * resolving object's (CR 603.7e).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellPlaneswalkerTypeChoice(gameData, entry.getControllerId());
            return;
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype type = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        var copies = (CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.thisTurn(
                entry.getControllerId(), entry.getCard(), copies.delayedAbility(type), entry.getSourcePermanentId()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " copies abilities of " + type.getDisplayName() + " planeswalkers activated this turn."));
        log.info("Game {} - {} creates a delayed copy of {} planeswalkers' abilities this turn",
                gameData.id, entry.getCard().getName(), type);
    }
}
