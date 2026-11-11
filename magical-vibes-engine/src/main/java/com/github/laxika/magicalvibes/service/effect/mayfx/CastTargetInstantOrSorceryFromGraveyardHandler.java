package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
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
                markOncePerTurnOnAcceptance(gameData, ability);
            }
            mayCastHandlerService.handleCastFromGraveyardChoice(gameData, player, accepted, ability, castFromGraveyardEffect);
        }
    }

    /**
     * "Do this only once each turn" (Emet-Selch of the Third Seat, CR 603.2h): the ability's opportunity is used up
     * only when the player agrees to cast, so declining leaves it available for a later event. The resolving entry
     * carries its ability's limit key, as its collector put it on the stack.
     */
    private void markOncePerTurnOnAcceptance(GameData gameData, PendingMayAbility ability) {
        if (ability.sourcePermanentId() == null || gameData.pendingEffectResolutionEntry == null) {
            return;
        }
        TriggeredAbilityStackService.markActionTaken(gameData, ability.sourcePermanentId(),
                gameData.pendingEffectResolutionEntry.getOnceEachTurnActionKey());
    }
}
