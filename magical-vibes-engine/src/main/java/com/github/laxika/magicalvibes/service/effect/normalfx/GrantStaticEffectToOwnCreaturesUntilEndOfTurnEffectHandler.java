package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect) effect;
        for (Permanent permanent : gameData.playerBattlefields.get(entry.getControllerId())) {
            if (!gameQueryService.isCreature(gameData, permanent)) continue;
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), new GrantEffectEffect(grant.staticEffect(), GrantScope.TARGET),
                permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}
