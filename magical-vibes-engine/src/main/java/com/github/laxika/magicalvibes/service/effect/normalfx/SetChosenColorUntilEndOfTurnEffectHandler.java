package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SetChosenColorUntilEndOfTurnEffect}: asks the controller for a color, then {@link #apply} makes the
 * permanent (or spell) that color until end of turn. When an earlier {@code ChooseColorAtResolutionEffect} of the same
 * resolution already chose a color ("becomes the color of your choice and gains hexproof from that color", Mondo
 * Gecko), that color is applied and nothing is asked; the later step that clears it is the card's.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetChosenColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetChosenColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetChosenColorUntilEndOfTurnEffect chosenColor = (SetChosenColorUntilEndOfTurnEffect) effect;
        UUID targetId = chosenColor.targeted() ? entry.getTargetId() : entry.getSourcePermanentId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null && (!chosenColor.canTargetSpell()
                || gameQueryService.findStackEntryByCardId(gameData, targetId) == null)) {
            return;
        }

        if (gameData.chosenSpellColor != null) {
            apply(gameData, targetId, gameData.chosenSpellColor, entry.getCard().getName(), entry.getControllerId());
            return;
        }
        playerInputService.beginColorSetChoice(gameData, entry.getControllerId(), targetId,
                entry.getCard().getName());
    }

    /**
     * Makes the permanent {@code targetId} (or the spell, when it is one on the stack) {@code color} until end of turn:
     * a floating layer-5 color-setting effect at its own timestamp (CR 613), and one log line.
     */
    public void apply(GameData gameData, UUID targetId, CardColor color, String sourceCardName, UUID controllerId) {
        String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceCardName, null,
                    controllerId, new GrantColorUntilEndOfTurnEffect(color), target.getId(), null, null,
                    Duration.UNTIL_END_OF_TURN, 0));
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " becomes " + colorName + " until end of turn."));
            log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
            return;
        }
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (targetSpell != null) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onCard(sourceCardName, null, controllerId,
                    new SpellColorUntilEndOfTurnEffect(color), targetId, Zone.STACK, Duration.UNTIL_END_OF_TURN));
            gameLogService.append(gameData, GameLog.cardThen(targetSpell.getCard(),
                    " becomes " + colorName + " until end of turn."));
            log.info("Game {} - spell {} becomes {} until end of turn",
                    gameData.id, targetSpell.getCard().getName(), colorName);
        }
    }
}
