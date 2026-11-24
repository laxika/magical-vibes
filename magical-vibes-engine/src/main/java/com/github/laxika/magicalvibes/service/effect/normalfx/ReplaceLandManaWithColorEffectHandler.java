package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves a {@link ReplaceLandManaWithColorEffect} given as an instruction ("until end of turn, if you tap a land
 * you control for mana, it produces {U} instead of any other type"): floats it for its duration, on every player
 * with the resolving object's controller as its "you", or on each land its subject names. It changes nothing now;
 * the land-mana replacement pool applies it as lands are tapped. A static one is never resolved.
 */
@Component
@RequiredArgsConstructor
public class ReplaceLandManaWithColorEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReplaceLandManaWithColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var replacement = (ReplaceLandManaWithColorEffect) effect;
        if (replacement.duration() == null) {
            return;
        }
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);
        if (replacement.subject() == null) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(sourceName, entry.getSourcePermanentId(),
                    entry.getControllerId(), replacement, null, replacement.duration()));
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": " + describe(replacement)));
            return;
        }
        List<Permanent> lands = subjectResolutionService.permanents(gameData, entry, effect, replacement.subject());
        for (Permanent land : lands) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceName, entry.getSourcePermanentId(),
                    entry.getControllerId(), replacement, land.getId(), replacement.duration()));
        }
    }

    private static String describe(ReplaceLandManaWithColorEffect replacement) {
        String produced = replacement.color() == null ? "mana of a color of your choice"
                : "{" + replacement.color().getCode() + "}";
        String instead = replacement.replacedTypes().isEmpty() ? " instead of any other type"
                : replacement.replacedTypes().containsAll(ManaColor.COLORS) ? " instead of any other color"
                : " instead of " + String.join(" or ", replacement.replacedTypes().stream().sorted()
                        .map(color -> "{" + color.getCode() + "}").toList());
        String amount = replacement.fixedAmount() == null ? "" : " (" + replacement.fixedAmount() + " mana)";
        String duration = Duration.UNTIL_END_OF_TURN.equals(replacement.duration()) ? " until end of turn" : "";
        return "affected lands tapped for mana produce " + produced + instead + amount + duration + ".";
    }
}
