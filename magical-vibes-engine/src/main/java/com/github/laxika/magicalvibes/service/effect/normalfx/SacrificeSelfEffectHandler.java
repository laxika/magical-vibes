package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sacrifices the stack entry's source permanent, or its triggering permanent for the triggering form, as a resolution
 * step. A player can sacrifice only a permanent they control (CR 701.21a), so the default form does nothing unless the
 * ability's controller still controls it, and nothing happens when it has left the battlefield or can't be
 * sacrificed. The step records its
 * {@link com.github.laxika.magicalvibes.model.DepartureResult} (none when it couldn't sacrifice anything) for an "if
 * you do" step after it, and a CR 616.1 question about how the permanent leaves ends the step
 * ({@link DepartureService#departAsStep}). The form with a token for the sacrificing player creates it for the
 * player who controlled the permanent as it was sacrificed, once it has left.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst,
        ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final PermanentControlSupport permanentControlSupport;
    private final SchemeAbandonSupport schemeAbandonSupport;
    private final DepartureService departureService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificeSelfEffect) effect;
        if (sacrifice.sacrificesSource() && entry.getCard() != null && entry.getCard().hasType(CardType.SCHEME)) {
            // A scheme's "abandon this scheme" (CR 701.33b), never a sacrifice.
            schemeAbandonSupport.abandon(gameData, entry.getCard(), entry.getSourcePermanentId());
            return;
        }
        Permanent self = subjectResolutionService.permanent(
                gameData, entry, effect, Subjects.of(sacrifice.sacrificed()));
        var currentControllerId = self == null ? null : gameQueryService.findPermanentController(gameData, self.getId());
        if (currentControllerId == null
                || !sacrifice.currentControllerSacrifices() && !currentControllerId.equals(entry.getControllerId())
                || gameQueryService.cantBeSacrificed(gameData, self)) {
            entry.recordDepartures(List.of());
            return;
        }

        if (sacrifice.tokenForSacrificingPlayer() == null) {
            departureService.departAsStep(gameData, entry, effect,
                    DepartureBatch.of(List.of(DepartureIntent.sacrifice(self.getId()))));
            return;
        }
        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, self.getId(), this);
    }

    /** "If the player does, they create …": the token goes to the player who sacrificed it. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificeSelfEffect) effect;
        for (DepartureResult result : entry.getResolutionResults().departures()) {
            if (result.sacrificed() && result.lastKnownControllerId() != null) {
                permanentControlSupport.applyCreateToken(gameData, result.lastKnownControllerId(),
                        sacrifice.tokenForSacrificingPlayer(), entry.getCard().getSetCode());
            }
        }
    }
}
