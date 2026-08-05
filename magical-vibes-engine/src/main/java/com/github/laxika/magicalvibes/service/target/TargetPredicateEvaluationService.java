package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Evaluates a {@link TargetPredicate} against one candidate of a known kind.
 *
 * <p>This is an <b>adapter, not a new evaluator</b>: each kind leaf delegates to the service that
 * already owns its predicate hierarchy — {@link PredicateEvaluationService} for permanents and
 * cards, {@link TargetLegalityService} for players and for stack entries on the targeting path.
 * No evaluation logic is duplicated here, so {@code FilterContext}, the CR 613.6 layer-4 verdict
 * memo and the deliberate split between the two stack-entry evaluators are all untouched. In
 * particular nothing here reaches {@code PredicateEvaluationService.matchesStaticFilter}, whose
 * whitelist throws on unsupported predicates and whose memo is keyed by filter instance.</p>
 *
 * <p>There is one method per {@link TargetPredicate.Kind} rather than a candidate sum type,
 * because that is how targeting already works: the caller knows which collection it is walking
 * (battlefield / players / graveyards / exile / stack) and asks whether this candidate is legal.
 * A predicate that carries no leaf of the asked-for kind rejects every candidate of that kind.</p>
 */
@Service
@RequiredArgsConstructor
public class TargetPredicateEvaluationService {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;

    /**
     * Whether {@code permanent} is a legal target.
     *
     * <p>{@code context} must carry a {@link GameData}: {@code PermanentIsCreaturePredicate} and
     * {@code PermanentIsLandPredicate} are layer-aware (CR 613.1d) only when it is present, and
     * fall back to raw card types plus animation flags when it is not — which silently
     * mis-handles an animated land. Targeting always has the game state at hand, so a missing one
     * is a programming error rather than something to degrade over.</p>
     */
    public boolean matchesPermanent(TargetPredicate predicate, Permanent permanent, FilterContext context) {
        requireGameData(context);
        return leaf(predicate, TargetPredicate.Kind.PERMANENT, TargetPredicate.Permanents.class)
                .map(perms -> predicateEvaluationService.matchesPermanentPredicate(permanent, perms.inner(), context))
                .orElse(false);
    }

    /** Whether the player {@code playerId} is a legal target for a source controlled by {@code controllerId}. */
    public boolean matchesPlayer(TargetPredicate predicate, UUID playerId, UUID controllerId, GameData gameData) {
        Objects.requireNonNull(gameData, "gameData");
        return leaf(predicate, TargetPredicate.Kind.PLAYER, TargetPredicate.Players.class)
                .map(players -> targetLegalityService.matchesPlayerPredicate(
                        gameData, controllerId, playerId, players.inner()))
                .orElse(false);
    }

    /**
     * Whether {@code card}, sitting in {@code graveyardOwnerId}'s graveyard, is a legal target for
     * a source controlled by {@code controllerId}. The leaf's {@link GraveyardSearchScope} decides
     * which graveyards are in range; the card predicate decides which cards within them.
     */
    public boolean matchesGraveyardCard(TargetPredicate predicate, Card card, UUID graveyardOwnerId,
                                        UUID controllerId, FilterContext context) {
        requireGameData(context);
        return leaf(predicate, TargetPredicate.Kind.GRAVEYARD_CARD, TargetPredicate.GraveyardCards.class)
                .map(graveyard -> inScope(graveyard.scope(), graveyardOwnerId, controllerId)
                        && predicateEvaluationService.matchesCardPredicate(
                                card, graveyard.inner(), context.sourceCardId()))
                .orElse(false);
    }

    /** Whether the exiled {@code card} is a legal target. */
    public boolean matchesExiledCard(TargetPredicate predicate, Card card, FilterContext context) {
        requireGameData(context);
        return leaf(predicate, TargetPredicate.Kind.EXILED_CARD, TargetPredicate.ExiledCards.class)
                .map(exiled -> predicateEvaluationService.matchesCardPredicate(
                        card, exiled.inner(), context.sourceCardId()))
                .orElse(false);
    }

    /**
     * Whether the stack entry {@code entry} is a legal target for a source controlled by
     * {@code controllerId}. {@code source} is the source permanent when the predicate is
     * source-relative (chosen-name), {@code null} otherwise.
     */
    public boolean matchesSpell(TargetPredicate predicate, StackEntry entry, UUID controllerId,
                                Permanent source, FilterContext context) {
        GameData gameData = requireGameData(context);
        return leaf(predicate, TargetPredicate.Kind.SPELL, TargetPredicate.Spells.class)
                .map(spells -> targetLegalityService.matchesStackEntryPredicate(
                        gameData, entry, spells.inner(), controllerId, source, context.xValue()))
                .orElse(false);
    }

    private static <T extends TargetPredicate.Leaf> Optional<T> leaf(TargetPredicate predicate,
                                                                     TargetPredicate.Kind kind,
                                                                     Class<T> type) {
        if (predicate == null) {
            return Optional.empty();
        }
        return predicate.leaf(kind).map(type::cast);
    }

    /**
     * Whether a graveyard belonging to {@code graveyardOwnerId} is within {@code scope} for a
     * source controlled by {@code controllerId}. An unknown controller puts every player-relative
     * scope out of range, mirroring how {@code matchesPlayerPredicate} treats a null controller.
     */
    private static boolean inScope(GraveyardSearchScope scope, UUID graveyardOwnerId, UUID controllerId) {
        return switch (scope) {
            case ALL_GRAVEYARDS -> true;
            case CONTROLLERS_GRAVEYARD -> controllerId != null && controllerId.equals(graveyardOwnerId);
            case OPPONENT_GRAVEYARD -> controllerId != null && !controllerId.equals(graveyardOwnerId);
        };
    }

    private static GameData requireGameData(FilterContext context) {
        if (context == null || context.gameData() == null) {
            throw new IllegalArgumentException(
                    "Target predicate evaluation needs a FilterContext carrying GameData — without it the "
                            + "creature/land leaves fall back to raw card types and mis-handle animated lands");
        }
        return context.gameData();
    }
}
