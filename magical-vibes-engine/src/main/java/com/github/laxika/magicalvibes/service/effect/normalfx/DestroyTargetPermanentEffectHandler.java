package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyTargetPermanentEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                // Capture the controller before destruction (needed for token creation)
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), destroy.cannotBeRegenerated());

                // Create token for the target's controller if specified
                if (destroy.tokenForController() != null && controllerId != null) {
                    destructionSupport.createTokenForPlayer(gameData, controllerId, destroy.tokenForController(), entry.getCard().getName());
                }
    }
}
