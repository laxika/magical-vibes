package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a destroy-target-creature effect with a token rider based on the creature's last-known stats. */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, target);
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);
        boolean destroyed = destructionSupport.tryDestroyAndLog(
                gameData, target, entry.getCard().getName());
        if (!destroyed) {
            return;
        }

        permanentControlSupport.applyCreateToken(
                gameData,
                entry.getControllerId(),
                e.tokenTemplate().withPowerToughness(power, toughness),
                1,
                entry.getCard().getSetCode());
    }
}
