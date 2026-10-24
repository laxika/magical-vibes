package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Sacrifices the source permanent on behalf of <em>its</em> controller and makes that same player
 * draw. The stack entry's controller is the activating opponent (Soul Ransom's ability is only
 * activatable by opponents), so neither half may use {@code entry.getControllerId()}.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfAndControllerDrawsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst,
        ContinuesAfterDeparture {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAndControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfAndControllerDrawsEffect) effect;

        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            // The source already left (destroyed in response, or sacrificed by an earlier activation):
            // its last controller still draws; only the sacrifice is impossible.
            UUID lastController = entry.getCard().getOwnerId() != null
                    ? entry.getCard().getOwnerId()
                    : gameQueryService.getOpponentId(gameData, entry.getControllerId());
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " can't be sacrificed — it is no longer on the battlefield."));
            playerInteractionSupport.applyDrawCards(gameData, lastController, e.cards());
            return;
        }

        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, self.getId(), this);
    }

    /** "… then draws": the player who controlled it as it was sacrificed draws, whether or not it left. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeSelfAndControllerDrawsEffect) effect;
        entry.getResolutionResults().departures().stream()
                .map(DepartureResult::lastKnownControllerId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .ifPresent(controllerId -> playerInteractionSupport.applyDrawCards(gameData, controllerId, e.cards()));
    }
}
