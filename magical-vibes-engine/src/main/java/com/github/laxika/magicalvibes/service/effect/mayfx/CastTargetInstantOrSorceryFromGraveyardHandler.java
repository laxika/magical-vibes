package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
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
     * "Do this only once each turn" (Emet-Selch of the Third Seat, CR 603.2h): the source's opportunity is used up
     * only when the player agrees to cast, so declining leaves it available for a later event. The limit is read from
     * the options of the source's ability with the cast effect, and marks the source as the limit's check reads it.
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
        boolean doOnlyOnceEachTurn = source.getCard().getTriggeredAbilities().stream()
                .anyMatch(triggered -> triggered.options().limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN
                        && triggered.effects().stream().anyMatch(registration -> castEffect.equals(registration.effect())
                        || registration.effect() instanceof MayEffect may && castEffect.equals(may.wrapped())));
        if (doOnlyOnceEachTurn) {
            gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
        }
    }
}
