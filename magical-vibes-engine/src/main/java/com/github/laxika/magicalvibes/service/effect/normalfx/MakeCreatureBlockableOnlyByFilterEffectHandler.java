package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeCreatureBlockableOnlyByFilterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatureBlockableOnlyByFilterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (MakeCreatureBlockableOnlyByFilterEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (target == null) {
            return;
        }

        CanBeBlockedOnlyByFilterEffect blockRestriction = new CanBeBlockedOnlyByFilterEffect(
                restriction.blockerPredicate(), restriction.allowedBlockersDescription());
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantEffectEffect(blockRestriction, GrantScope.TARGET),
                target.getId(), null, null, restriction.duration(), 0));

        String duration = restriction.duration() == EffectDuration.UNTIL_END_OF_COMBAT
                ? " this combat" : "";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " can be blocked" + duration + " only by " + restriction.allowedBlockersDescription() + "."));
        log.info("Game {} - {} can be blocked{} only by {}", gameData.id,
                target.getCard().getName(), duration, restriction.allowedBlockersDescription());
    }
}
