package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SimultaneouslyFlipAllPermanentsTapStatesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link SimultaneouslyFlipAllPermanentsTapStatesEffect}: the subject's permanents are found and split by
 * tap state before either half is applied.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimultaneouslyFlipAllPermanentsTapStatesEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final TapUntapSupport tapUntapSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SimultaneouslyFlipAllPermanentsTapStatesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SimultaneouslyFlipAllPermanentsTapStatesEffect) effect;
        List<Permanent> toUntap = new ArrayList<>();
        List<Permanent> toTap = new ArrayList<>();
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, e.permanents())) {
            if (permanent.isTapped()) {
                toUntap.add(permanent);
            } else {
                toTap.add(permanent);
            }
        }

        int flipped = 0;
        for (Permanent permanent : toUntap) {
            if (tapUntapSupport.untapPermanent(gameData, permanent)) {
                flipped++;
            }
        }
        for (Permanent permanent : toTap) {
            if (tapUntapSupport.tapPermanent(gameData, permanent)) {
                flipped++;
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" simultaneously flips the tap state of " + flipped + " permanent(s).")
                .build());
        log.info("Game {} - {} flips tap state of {} permanents",
                gameData.id,
                entry.getCard() != null ? entry.getCard().getName() : "effect",
                flipped);
    }
}
