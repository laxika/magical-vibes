package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetOpponentMayDrawCardEffect}: queues the optional draw for the resolving
 * controller's opponent, so that player is the one asked.
 */
@Component
@RequiredArgsConstructor
public class TargetOpponentMayDrawCardEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentMayDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var draw = (TargetOpponentMayDrawCardEffect) effect;
        for (UUID opponentId : subjectResolutionService.players(gameData, entry, effect, draw.opponent())) {
            // The opponent chooses while the ability resolves (CR 608.2d), not as an ability of its own.
            gameData.queueMayAbilityForPlayer(entry.getCard(), entry.getControllerId(),
                    new MayEffect(new DrawCardEffect(1, Subjects.targetPlayerDeclaredElsewhere()), "Draw a card?"),
                    opponentId, entry.getSourcePermanentId(), opponentId, entry.getSourcePermanentSnapshot());
            gameLogService.append(gameData, GameLog.textCardText(
                    "", entry.getCard(), " offers " + gameData.playerIdToName.get(opponentId) + " a card."));
        }
    }
}
