package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PhaseOutEffect}: {@link SubjectResolutionService} names the permanents, which phase out together
 * through {@link PhasingService}, which owns attachments (CR 702.26g), combat removal (CR 506.4) and phasing back in.
 * If the subject names nothing on the battlefield — the source or target already left, or the source Aura is
 * unattached — the ability does nothing. A set ("all lands you control") also logs how many phased out.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhaseOutEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PhasingService phasingService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PhaseOutEffect e = (PhaseOutEffect) effect;
        List<Permanent> toPhaseOut = subjectResolutionService.permanents(gameData, entry, effect, e.subject());
        if (toPhaseOut.isEmpty()) {
            return;
        }
        phasingService.phaseOut(gameData, toPhaseOut);
        if (e.subject().isSet()) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(String.format(" phases out %d permanent(s).", toPhaseOut.size()))
                    .build());
            log.info("Game {} - {} phases out {} permanents", gameData.id,
                    entry.getCard() != null ? entry.getCard().getName() : "effect", toPhaseOut.size());
        }
    }
}
