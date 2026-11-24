package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect.ColorChoice;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link BecomeChosenColorsUntilEndOfTurnEffect}: finds the permanents its subject names once (CR 611.2c),
 * then floats the fixed colours on each, or asks the controller for the colours first (the choice handler then calls
 * {@link #apply}). Nothing is asked when the subject names no permanent.
 */
@Component
@RequiredArgsConstructor
public class BecomeChosenColorsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeChosenColorsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var becomes = (BecomeChosenColorsUntilEndOfTurnEffect) effect;
        List<UUID> affected = subjectResolutionService.permanents(gameData, entry, effect, becomes.subject())
                .stream().map(Permanent::getId).toList();
        if (affected.isEmpty()) {
            return;
        }
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        if (becomes.choice() == ColorChoice.FIXED) {
            apply(gameData, affected, sourceName, entry.getControllerId(), becomes.colors(),
                    becomes.duration());
            return;
        }
        playerInputService.beginBecomeChosenColorsChoice(gameData, entry.getControllerId(), affected, sourceName,
                List.of(), becomes.duration(), becomes.choice() == ColorChoice.ONE);
    }

    /**
     * Floats "becomes exactly {@code colors}" (none: colorless) on each of {@code permanentIds} still on the
     * battlefield, for {@code duration}, and logs one line per permanent.
     */
    public void apply(GameData gameData, Collection<UUID> permanentIds, String sourceName, UUID controllerId,
                      Set<CardColor> colors, Duration duration) {
        String suffix = " becomes " + describe(colors)
                + (Duration.UNTIL_END_OF_TURN.equals(duration) ? " until end of turn." : ".");
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                    controllerId, new BecomeChosenColorsUntilEndOfTurnEffect(colors), permanent.getId(), null, null,
                    duration, 0));
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), suffix));
        }
    }

    private static String describe(Set<CardColor> colors) {
        if (colors.isEmpty()) {
            return "colorless";
        }
        if (colors.size() == CardColor.values().length) {
            return "all colors";
        }
        return colors.stream()
                .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                .reduce((a, b) -> a + " and " + b).orElse("");
    }
}
