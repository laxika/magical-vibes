package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCombatOpponentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Applies a perpetual power/toughness change to the referenced combat opponent. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        PerpetuallyBoostCombatOpponentEffect boost = (PerpetuallyBoostCombatOpponentEffect) effect;
        var copy = target.getCard().createRuntimeCopy();
        if (copy.getPower() != null) {
            copy.setPower(copy.getPower() + boost.powerBoost());
        }
        if (copy.getToughness() != null) {
            copy.setToughness(copy.getToughness() + boost.toughnessBoost());
        }
        copy.freeze();
        target.exchangeCard(copy);
    }
}
