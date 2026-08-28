package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyGrantingPermanentIfNoCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyGrantingPermanentIfNoCountersEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyGrantingPermanentIfNoCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyGrantingPermanentIfNoCountersEffect) effect;
        if (destroy.grantingPermanentId() == null) {
            return;
        }
        Permanent grantingPermanent = gameQueryService.findPermanentById(gameData, destroy.grantingPermanentId());
        if (grantingPermanent == null
                || grantingPermanent.getCounterCount(destroy.counterType()) != 0) {
            return;
        }
        destructionSupport.destroyBatch(
                gameData, java.util.List.of(grantingPermanent), entry.getCard().getName(), false);
    }
}
