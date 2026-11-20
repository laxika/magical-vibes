package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantSupertypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSupertypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantSupertypeUntilEndOfTurnEffect) effect;
        if (grant.scope() != GrantScope.SELF && grant.scope() != GrantScope.ENCHANTED_PERMANENT
                && grant.scope() != GrantScope.TARGET) {
            return;
        }
        List<Permanent> targets = subjectResolutionService.permanents(gameData, entry, effect, grant.scope().subject());

        for (Permanent target : targets) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(), grant,
                    target.getId(), null, null,
                    Duration.UNTIL_END_OF_TURN, 0));
            String supertypeName = grant.supertype().getDisplayName().toLowerCase();
            String text = grant.gained()
                    ? " gains " + supertypeName + " until end of turn."
                    : " is no longer " + supertypeName + " until end of turn.";
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(text).build());
        }
    }
}
