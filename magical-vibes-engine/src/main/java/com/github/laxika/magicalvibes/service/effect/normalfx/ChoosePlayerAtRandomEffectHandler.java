package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerAtRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Picks the player and records {@link StepKind#CHOOSE}. */
@Component
@RequiredArgsConstructor
public class ChoosePlayerAtRandomEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChoosePlayerAtRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChoosePlayerAtRandomEffect) effect;
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, choose.players());
        if (players.isEmpty()) {
            entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
            return;
        }
        UUID chosen = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        entry.recordStep(StepKind.CHOOSE, StepOutcome.ofAmount(1, List.of(chosen)));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " chooses " + gameData.playerIdToName.get(chosen) + " at random."));
    }
}
