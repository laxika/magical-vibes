package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantControllerAndPermanentsHexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link GrantControllerAndPermanentsHexproofFromColorsEffect}: fixes the colors (the one chosen earlier in
 * the resolution for the chosen form, CR 608.2h), then floats a copy carrying them on the controller when asked and on
 * each permanent the subject names as it resolves (CR 611.2c), until end of turn.
 */
@Component
@RequiredArgsConstructor
public class GrantControllerAndPermanentsHexproofFromColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerAndPermanentsHexproofFromColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantControllerAndPermanentsHexproofFromColorsEffect) effect;
        Set<CardColor> colors = e.ofChosenColor()
                ? (gameData.chosenSpellColor == null ? Set.of() : Set.of(gameData.chosenSpellColor))
                : e.colors();
        if (colors.isEmpty()) {
            return;
        }
        var granted = new GrantControllerAndPermanentsHexproofFromColorsEffect(colors);
        UUID controllerId = entry.getControllerId();
        String colorNames = colors.stream()
                .map(color -> color.name().toLowerCase())
                .sorted()
                .reduce((first, second) -> first + " or " + second)
                .orElse("");

        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, e.permanents());
        if (e.controllerToo()) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), controllerId, granted, controllerId, Duration.UNTIL_END_OF_TURN));
        }
        for (Permanent permanent : permanents) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(entry.getCard().getName(),
                    entry.getSourcePermanentId(), controllerId, granted, permanent.getId(), Duration.UNTIL_END_OF_TURN));
            if (!e.controllerToo()) {
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                        " gains hexproof from " + colorNames + " until end of turn."));
            }
        }
        if (e.controllerToo()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " and permanents they control gain hexproof from " + colorNames + " until end of turn."));
        }
    }
}
