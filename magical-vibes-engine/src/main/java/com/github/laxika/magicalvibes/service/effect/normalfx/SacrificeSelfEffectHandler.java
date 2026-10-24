package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sacrifices the stack entry's source permanent (CR 701.21a) as a resolution step: the step records its
 * {@link com.github.laxika.magicalvibes.model.DepartureResult} (none when it couldn't sacrifice anything) for an "if
 * you do" step after it, and a CR 616.1 question about how the permanent leaves ends the step
 * ({@link DepartureService#departAsStep}). The form with a token for the sacrificing player still sacrifices
 * through the legacy wrapper, so its token follows the sacrifice at once.
 */
@Component
@RequiredArgsConstructor
public class SacrificeSelfEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentControlSupport permanentControlSupport;
    private final SchemeAbandonSupport schemeAbandonSupport;
    private final DepartureService departureService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificeSelfEffect) effect;
        if (entry.getCard() != null && entry.getCard().hasType(CardType.SCHEME)) {
            // A scheme's "abandon this scheme" (CR 701.33b), never a sacrifice.
            schemeAbandonSupport.abandon(gameData, entry.getCard(), entry.getSourcePermanentId());
            return;
        }
        Permanent self = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        var currentControllerId = self == null ? null : gameQueryService.findPermanentController(gameData, self.getId());
        if (currentControllerId == null
                || !sacrifice.currentControllerSacrifices() && !entry.getControllerId().equals(currentControllerId)
                || gameQueryService.cantBeSacrificed(gameData, self)) {
            entry.recordDepartures(List.of());
            return;
        }

        if (sacrifice.tokenForSacrificingPlayer() == null) {
            departureService.departAsStep(gameData, entry, effect,
                    DepartureBatch.of(List.of(DepartureIntent.sacrifice(self.getId()))));
            return;
        }

        if (permanentRemovalService.sacrificePermanentToGraveyard(gameData, self)) {
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                    gameData, currentControllerId, self.getCard());
            gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " is sacrificed."));
            permanentRemovalService.removeOrphanedAuras(gameData);
            permanentControlSupport.applyCreateToken(gameData, currentControllerId,
                    sacrifice.tokenForSacrificingPlayer(), entry.getCard().getSetCode());
        }
    }
}
