package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeGrantingPermanentAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.model.DepartureResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeGrantingPermanentAndControllerDrawsEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeGrantingPermanentAndControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeGrantingPermanentAndControllerDrawsEffect) effect;
        UUID grantingPermanentId = e.grantingPermanentId();
        if (grantingPermanentId == null) {
            return;
        }

        Permanent grantingPermanent = gameQueryService.findPermanentById(gameData, grantingPermanentId);
        if (grantingPermanent == null) {
            // Nothing is sacrificed, but the draw is not contingent on it: the controller of the
            // granting permanent as of activation still draws.
            if (e.grantingControllerId() != null) {
                playerInteractionSupport.applyDrawCards(gameData, e.grantingControllerId(), e.cards());
            }
            return;
        }

        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, grantingPermanentId, this);
    }

    /** "… sacrifices it and draws": the player who controlled it as it was sacrificed draws. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeGrantingPermanentAndControllerDrawsEffect) effect;
        entry.getResolutionResults().departures().stream()
                .map(DepartureResult::lastKnownControllerId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .ifPresent(controllerId -> playerInteractionSupport.applyDrawCards(gameData, controllerId, e.cards()));
    }
}
