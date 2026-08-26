package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMayTakeDamageOrBoostSelfEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one player's Barbarian Bully damage-or-boost choice. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMayTakeDamageOrBoostSelfHandler implements MayEffectHandlerBean {

    private final AnyPlayerMayTakeDamageOrBoostSelfEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMayTakeDamageOrBoostSelfEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMayTakeDamageOrBoostSelfEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            effectHandler.dealDamage(gameData, ability, effect, chooserId);
        } else {
            List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);
            if (remaining.isEmpty()) {
                effectHandler.boostSelf(gameData, ability.sourceCard(), effect,
                        ability.sourcePermanentSnapshot());
            } else {
                effectHandler.promptNext(gameData, ability.sourceCard(),
                        new AnyPlayerMayTakeDamageOrBoostSelfEffect(
                                effect.damage(), effect.powerBoost(), effect.toughnessBoost(),
                                remaining, effect.abilityControllerId(), effect.sourcePermanentId()),
                        ability.sourcePermanentSnapshot());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
