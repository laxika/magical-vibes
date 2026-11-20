package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCardTypeFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveCardTypeFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCardTypeFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveCardTypeFromTargetPermanentEffect) effect;
        Permanent target = subjectResolutionService.permanent(gameData, entry, effect,
                remove.scope() == GrantScope.SELF ? GrantScope.SELF.subject() : GrantScope.TARGET.subject());
        if (target == null) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(), remove,
                target.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));

        String typeName = remove.cardType().name().charAt(0)
                + remove.cardType().name().substring(1).toLowerCase();
        gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                .text(" isn't an " + typeName + " until end of turn.").build());
    }
}
