package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cast-from-graveyard — e.g. Chancellor of the Spires.
 */
@Component
@RequiredArgsConstructor
public class CastTargetInstantOrSorceryFromGraveyardHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastTargetInstantOrSorceryFromGraveyardEffect castFromGraveyardEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElse(null);
        if (castFromGraveyardEffect != null) {
            if (accepted) {
                markOncePerTurnOnAcceptance(gameData, ability, castFromGraveyardEffect);
            }
            mayCastHandlerService.handleCastFromGraveyardChoice(gameData, player, accepted, ability, castFromGraveyardEffect);
        }
    }

    /**
     * "Do this only once each turn" (Emet-Selch of the Third Seat): the source's opportunity is used up
     * only when the player agrees to cast, so declining leaves it available for a later event.
     */
    private void markOncePerTurnOnAcceptance(GameData gameData, PendingMayAbility ability,
                                             CastTargetInstantOrSorceryFromGraveyardEffect castEffect) {
        if (ability.sourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (source == null) {
            return;
        }
        for (EffectSlot slot : EffectSlot.values()) {
            for (CardEffect effect : source.getCard().getEffects(slot)) {
                if (effect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()
                        && castEffect.equals(once.wrapped())) {
                    if (once.key() == null) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
                    } else {
                        gameData.keyedOncePerTurnTriggersFiredThisTurn
                                .computeIfAbsent(source.getId(),
                                        ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                                .add(once.key());
                    }
                    return;
                }
            }
        }
    }
}
