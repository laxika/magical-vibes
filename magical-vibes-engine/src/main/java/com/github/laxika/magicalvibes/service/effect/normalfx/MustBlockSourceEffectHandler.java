package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustBlockSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustBlockSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MustBlockSourceEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        // Activated abilities snapshot their source onto the effect; a triggered ability names it on the stack entry:
        // its source, or the permanent the trigger event was about (an Equipment's "block it", Grappling Hook).
        UUID sourceId = e.sourcePermanentId() != null ? e.sourcePermanentId()
                : subjectResolutionService.permanentIds(gameData, entry, effect, e.blocked()).stream()
                        .findFirst().orElse(null);
        if (target == null || sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        String sourceName = source != null ? FaceDownVisibility.publicName(gameData, source) : FaceDownVisibility.sourceName(gameData, entry);

        gameData.addEffectOnPermanent(entry, new MustBlockSourceEffect(sourceId, null, e.untilEndOfCombat()),
                target.getId(), e.untilEndOfCombat() ? Duration.UNTIL_END_OF_COMBAT : Duration.UNTIL_END_OF_TURN);

        
        String duration = e.untilEndOfCombat() ? "this combat" : "this turn";
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" must block " + sourceName + " " + duration + " if able.").build());

        log.info("Game {} - {} must block {} this turn if able", gameData.id, target.getCard().getName(), sourceName);
    }
}
