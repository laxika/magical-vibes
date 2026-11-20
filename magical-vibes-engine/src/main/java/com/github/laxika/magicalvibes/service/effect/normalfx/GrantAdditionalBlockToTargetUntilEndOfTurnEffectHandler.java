package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class GrantAdditionalBlockToTargetUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalBlockToTargetUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantAdditionalBlockToTargetUntilEndOfTurnEffect) effect;

        PermanentSubject subject = grant.scope() == GrantScope.SELF ? GrantScope.SELF.subject() : GrantScope.TARGET.subject();
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
            gameData.addEffectOnPermanent(entry, grant, target.getId(), Duration.UNTIL_END_OF_TURN);

            gameLogService.append(gameData, GameLog.builder()
                    .card(target.getCard())
                    .text(String.format(" can block %d additional creature(s) this turn.", grant.additionalBlocks()))
                    .build());

            log.info("Game {} - {} can block {} additional creature(s) this turn",
                    gameData.id, target.getCard().getName(), grant.additionalBlocks());
        }
    }
}
