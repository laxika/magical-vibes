package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.rules.CrewEvent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BecomeSaddledUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeSaddledUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var saddle = (BecomeSaddledUntilEndOfTurnEffect) effect;
        // "It becomes saddled" (Kolodin) is the triggering permanent, where it is now.
        if (saddle.scope() != GrantScope.SELF && saddle.scope() != GrantScope.TRIGGERING_PERMANENT
                && saddle.scope() != GrantScope.TARGET) {
            return;
        }

        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, saddle.scope().subject())) {
            boolean becameSaddled = !target.isSaddled();
            target.setSaddled(true);
            if (becameSaddled) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                if (controllerId != null) {
                    gameData.rulesEvents.occur(new CrewEvent.BecameSaddled(target, controllerId));
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" becomes saddled until end of turn.").build());
        }
    }
}
