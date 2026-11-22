package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantPlayChosenNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves {@link CantPlayChosenNameUntilNextTurnEffect}: one floating rule about the name the resolution keeps. */
@Component
@RequiredArgsConstructor
public class CantPlayChosenNameUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantPlayChosenNameUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var rule = (CantPlayChosenNameUntilNextTurnEffect) effect;
        String name = entry.getChosenCardName();
        if (name == null) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        CantPlayChosenNameUntilNextTurnEffect named = rule.named(name);
        String who;
        if (rule.landsToo()) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer("Named spell and land lock", null,
                    controllerId, named, null, Duration.UNTIL_YOUR_NEXT_TURN));
            who = "Spells and lands named " + name + " can't be cast or played";
        } else {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, rule.players())) {
                gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer("Named spell lock", null, controllerId,
                        named, playerId, Duration.UNTIL_YOUR_NEXT_TURN));
            }
            who = "Opponents can't cast spells named " + name;
        }
        gameLogService.append(gameData, GameLog.text(
                who + " until " + gameData.playerIdToName.get(controllerId) + "'s next turn."));
    }
}
