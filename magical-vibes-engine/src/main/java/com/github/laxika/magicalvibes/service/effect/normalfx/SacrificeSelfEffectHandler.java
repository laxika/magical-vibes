package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
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
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Sacrifices the permanent the effect's subject names (the source, the triggering, the attached or the target
 * permanent, found by {@link SubjectResolutionService}) as a resolution step. The effect's sacrificer is told to
 * sacrifice it, and a player can sacrifice only a permanent they control (CR 701.21a), so nothing happens unless the
 * sacrificer controls it as the effect resolves; nor when it has left the battlefield, can't be sacrificed, is a target
 * that is no longer legal (CR 608.2b), or the sacrificer's permanents can't be sacrificed by the effect's controller's
 * spells and abilities (Sigarda, Host of Herons). The step records its
 * {@link com.github.laxika.magicalvibes.model.DepartureResult} (none when it couldn't sacrifice anything) for an "if
 * you do" step after it, and a CR 616.1 question about how the permanent leaves ends the step
 * ({@link DepartureService#departAsStep}). The form with a token for the sacrificing player creates it for the
 * player who controlled the permanent as it was sacrificed, once it has left. A {@code thisWay(CHOOSE)} subject names
 * every permanent chosen ("that player sacrifices all permanents in the pile of their choice"): those of them the
 * sacrificer controls leave as one event (CR 101.4).
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
    private final PredicateEvaluationService predicateEvaluationService;

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
        if (sacrifice.subject() instanceof PermanentSubject.ThisWay) {
            sacrificeAllChosen(gameData, entry, sacrifice);
            return;
        }
        Permanent sacrificed = subjectResolutionService.permanent(gameData, entry, effect, sacrifice.subject());
        UUID currentControllerId = sacrificed == null ? null
                : gameQueryService.findPermanentController(gameData, sacrificed.getId());
        UUID sacrificerId = currentControllerId == null ? null
                : subjectResolutionService.player(gameData, entry, effect, sacrifice.sacrificer());
        if (currentControllerId == null || !currentControllerId.equals(sacrificerId)
                || sacrifice.subject().declaresTarget() && !isStillLegalTarget(gameData, entry, sacrifice, sacrificed)
                || gameQueryService.cantBeSacrificed(gameData, sacrificed)
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificerId, entry.getControllerId())) {
            entry.recordDepartures(List.of());
            return;
        }

        if (sacrifice.subject().declaresTarget()) {
            rememberTarget(gameData, entry, sacrificed, currentControllerId);
        }
        if (sacrifice.tokenForSacrificingPlayer() == null) {
            departureService.departAsStep(gameData, entry, effect,
                    DepartureBatch.of(List.of(DepartureIntent.sacrifice(sacrificed.getId()))));
            return;
        }
        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, sacrificed.getId(), this);
    }

    /** Every chosen permanent the sacrificer controls and can sacrifice, as one batch. */
    private void sacrificeAllChosen(GameData gameData, StackEntry entry, SacrificeSelfEffect sacrifice) {
        UUID sacrificerId = subjectResolutionService.player(gameData, entry, sacrifice, sacrifice.sacrificer());
        List<DepartureIntent> intents = sacrificerId == null
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificerId, entry.getControllerId())
                ? List.of()
                : subjectResolutionService.permanents(gameData, entry, sacrifice, sacrifice.subject()).stream()
                        .filter(permanent -> sacrificerId.equals(
                                gameQueryService.findPermanentController(gameData, permanent.getId())))
                        .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                        .map(permanent -> DepartureIntent.sacrifice(permanent.getId()))
                        .toList();
        if (intents.isEmpty()) {
            entry.recordDepartures(List.of());
            return;
        }
        departureService.departAsStep(gameData, entry, sacrifice, DepartureBatch.of(intents));
    }

    /**
     * Records the target's controller, card, power and toughness as it is sacrificed, so "its controller" and "that
     * creature's power" of a later step read them once it has left (the removal service records them only for entries
     * on the stack, not for a resolving loyalty ability).
     */
    private void rememberTarget(GameData gameData, StackEntry entry, Permanent target, UUID controllerId) {
        entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
        entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
        entry.getLastKnownPermanentPowers().put(target.getId(), gameQueryService.getEffectivePower(gameData, target));
        entry.getLastKnownPermanentToughnesses().put(target.getId(),
                gameQueryService.getEffectiveToughness(gameData, target));
    }

    /**
     * A target that no longer matches what it was chosen as is illegal and isn't affected (CR 608.2b); a spell with
     * another, still legal target resolves without it.
     */
    private boolean isStillLegalTarget(GameData gameData, StackEntry entry, SacrificeSelfEffect sacrifice,
                                       Permanent target) {
        if (sacrifice.targetFilter() == null) {
            return gameQueryService.isCreature(gameData, target);
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withTriggeringPlayerId(entry.getTriggeringPlayerId());
        return predicateEvaluationService.matchesPermanentPredicate(target, sacrifice.targetFilter(), filterContext);
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
