package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayRansomEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Ends a ransom control effect when the targeted creature's owner pays its ransom. */
@Component
@RequiredArgsConstructor
public class PayRansomEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayRansomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var ransom = (PayRansomEffect) effect;
        UUID targetId = entry.getSourcePermanentId();
        if (targetId == null || ransom.ransomSourcePermanentId() == null) {
            return;
        }

        gameData.expireFloatingEffects(floating ->
                ransom.ransomSourcePermanentId().equals(floating.sourcePermanentId())
                        && targetId.equals(floating.affectedPermanentId())
                        && (floating.isControlEffect()
                        || floating.effect() instanceof GrantActivatedAbilityEffect));

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            creatureControlService.recomputeControl(gameData, target);
        }
    }
}
