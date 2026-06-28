package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyAllPermanentsEffect) effect;
        List<Permanent> toDestroy = new ArrayList<>();
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(entry.getCard().getId())
                        .withSourceControllerId(entry.getControllerId());

                gameData.forEachBattlefield((playerId, battlefield) -> {
                    for (Permanent perm : battlefield) {
                        if (gameQueryService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                            toDestroy.add(perm);
                        }
                    }
                });

                destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), e.cannotBeRegenerated());
    }
}
