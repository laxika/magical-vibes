package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnteringCreatureFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnteringCreatureFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        // "That creature" is the permanent whose entry triggered the ability; when the source itself
        // entered, the trigger carries no separate triggering permanent, so the source is the fighter.
        UUID fighterId = entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId()
                : entry.getSourcePermanentId();
        if (fighterId == null) return;

        fightSupport.fight(gameData, entry,
                gameQueryService.findPermanentById(gameData, fighterId),
                gameQueryService.findPermanentById(gameData, targetId));
    }
}
