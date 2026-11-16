package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;

/**
 * Resolves {@link SacrificeTriggeringPermanentEffect}: sacrifices the triggering permanent when the
 * ability's controller still controls it (CR 701.21a). Never targets, so an absent permanent is a
 * silent no-op.
 */
@Component
@RequiredArgsConstructor
public class SacrificeTriggeringPermanentEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringId = entry.getTriggeringPermanentId();
        if (triggeringId == null) {
            return;
        }
        Permanent triggering = gameQueryService.findPermanentById(gameData, triggeringId);
        if (triggering == null) {
            return;
        }
        var sacrifice = (SacrificeTriggeringPermanentEffect) effect;
        if (!sacrifice.currentControllerSacrifices() && !Objects.equals(entry.getControllerId(),
                gameQueryService.findPermanentController(gameData, triggeringId))) {
            return;
        }

        if (sacrifice.tokenForSacrificingPlayer() == null) {
            stepDepartureSupport.sacrifice(gameData, entry, effect, triggeringId);
            return;
        }
        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, triggeringId, this);
    }

    /** "If the player does, they create ...": the token goes to the player who sacrificed it. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificeTriggeringPermanentEffect) effect;
        for (DepartureResult result : entry.getResolutionResults().departures()) {
            if (result.sacrificed() && result.lastKnownControllerId() != null) {
                permanentControlSupport.applyCreateToken(gameData, result.lastKnownControllerId(),
                        sacrifice.tokenForSacrificingPlayer(), entry.getCard().getSetCode());
            }
        }
    }
}
