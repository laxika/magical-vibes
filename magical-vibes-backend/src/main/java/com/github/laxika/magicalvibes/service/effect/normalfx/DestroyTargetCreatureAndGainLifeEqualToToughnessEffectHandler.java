package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureAndGainLifeEqualToToughnessEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureAndGainLifeEqualToToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetCreatureAndGainLifeEqualToToughnessEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                int toughness = gameQueryService.getEffectiveToughness(gameData, target);

                // Capture whether the life-gain condition is met before destruction removes the permanent
                boolean gainsLife = e.lifeGainCondition() == null
                        || gameQueryService.matchesPermanentPredicate(gameData, target, e.lifeGainCondition());

                // Attempt to destroy (life gain, when applicable, happens regardless of destruction result)
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

                // Gain life equal to toughness regardless of destruction result
                if (gainsLife) {
                    lifeSupport.applyGainLife(gameData, entry.getControllerId(), toughness,
                            "equal to " + target.getCard().getName() + "'s toughness");
                }
    }
}
