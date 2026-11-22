package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var chosenEffect = (TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect) effect;
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(
                    gameData, entry.getControllerId(), chosenEffect.excludedSubtypes());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        if (chosenEffect.subject().isSet()) {
            int affectedCount = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    chosenEffect.subject())) {
                addCreatureType(gameData, entry, chosenEffect, permanent, chosenSubtype);
                affectedCount++;
            }
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" makes " + affectedCount + " creature(s) into "
                            + chosenSubtype.getDisplayName() + "s until end of turn.")
                    .build());
            return;
        }

        Permanent target = subjectResolutionService.permanent(gameData, entry, effect, chosenEffect.subject());
        if (target == null) {
            return;
        }

        addCreatureType(gameData, entry, chosenEffect, target, chosenSubtype);
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" becomes a " + chosenSubtype.getDisplayName() + " until end of turn.")
                .build());
    }

    private void addCreatureType(GameData gameData, StackEntry entry,
                                 TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect effect, Permanent permanent,
                                 CardSubtype chosenSubtype) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                entry.getControllerId(), new GrantSubtypeEffect(chosenSubtype, Subjects.target(), !effect.retainOtherTypes()),
                permanent.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
    }
}
