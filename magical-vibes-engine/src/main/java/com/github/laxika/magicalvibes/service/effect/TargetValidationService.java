package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TargetValidationService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetValidatorRegistry registry;

    public TargetValidationService(GameQueryService gameQueryService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   TargetValidatorRegistry registry) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.registry = registry;
    }

    public Optional<String> checkEffectTargets(List<CardEffect> effects, TargetValidationContext context) {
        for (CardEffect effect : effects) {
            CardEffect effectToValidate = effect;
            // Unwrap replacement conditional effects to validate the inner effects.
            // Both paths share the same targeting, so validate the base effect.
            if (effect instanceof ConditionalReplacementEffect replacement) {
                effectToValidate = replacement.baseEffect();
            }
            // The declarative TargetSpec interpreter runs FIRST for every context (it lives in the
            // service, not as a scanned @ValidatesTarget bean, so contexts that build the registry
            // outside Spring still get it). A registered class validator, when present, runs after
            // it as the escape hatch for logic the spec cannot express.
            TargetSpec spec = effectToValidate.targetSpec();
            TargetValidator validator = registry.getValidator(effectToValidate);
            try {
                boolean spellTargetPath = context.targetZone() == Zone.STACK
                        && EffectResolution.targetsSpellOnStack(effectToValidate);
                if (spec.targetPredicate() != null && !spellTargetPath) {
                    validateSpec(context, spec, effectToValidate);
                }
                if (validator != null) {
                    validator.validate(context, effectToValidate);
                }
            } catch (IllegalStateException e) {
                return Optional.of(e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * Interprets a declarative {@link TargetSpec} into the same structural target checks the
     * hand-written {@code @ValidatesTarget} validators perform. Called only when the spec targets
     * something. Semantics mirror the corresponding validators exactly (see
     * {@code DamageTargetValidators} / {@code DestructionTargetValidators}).
     *
     * <p>Everything about <em>which</em> permanent is legal comes from the spec's
     * {@link TargetSpec#targetPredicate()}: the declared target's own restriction and the narrowing
     * predicate are one composed {@code PermanentPredicate} there, evaluated by the service that
     * owns that hierarchy. Two consequences, both rules-correct and both deliberate (CR 613.1d,
     * layer 4 — type-changing effects are applied before targeting legality is judged): "target
     * land" now accepts a permanent a type-<em>replacing</em> effect turned into a land, and "any
     * target" now rejects a planeswalker that stopped being one.</p>
     *
     * <p>The zone-wide graveyard gate (Ground Seal) stays here rather than in the predicate: it is
     * a property of the board, not of the candidate. So is the CR 702.16b protection check, which
     * rides on the orthogonal {@link TargetSpec#harmful()} axis.</p>
     */
    private void validateSpec(TargetValidationContext ctx, TargetSpec spec, CardEffect effect) {
        TargetPredicate predicate = spec.targetPredicate();

        if (predicate.admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                && !gameQueryService.canGraveyardCardsBeTargeted(ctx.gameData())) {
            throw new IllegalStateException("Cards in graveyards can't be the targets of spells or abilities");
        }

        PermanentPredicate restriction = predicate.permanentRestriction().orElse(null);
        if (restriction != null && demandsPermanentTarget(predicate, restriction, effect)) {
            requireTarget(ctx);
            boolean playerTarget = predicate.admits(TargetPredicate.Kind.PLAYER)
                    && ctx.gameData().playerIds.contains(ctx.targetId());
            if (!playerTarget) {
                requireBattlefieldTarget(ctx);
            }
        }

        Permanent target = ctx.targetId() == null
                ? null
                : gameQueryService.findPermanentById(ctx.gameData(), ctx.targetId());
        if (target == null) {
            return;
        }
        if (restriction != null) {
            FilterContext filterContext = sourceFilterContext(ctx);
            if (!predicateEvaluationService.matchesPermanentPredicate(target, restriction, filterContext)) {
                throw new IllegalStateException(rejectionMessage(predicate, restriction, target, filterContext));
            }
        }
        if (spec.harmful()) {
            checkProtection(ctx, target);
        }
    }

    /**
     * Whether the spec insists that a target be supplied at all. Two shapes say no:
     *
     * <ul>
     *   <li>An effect that distributes an announced amount among its targets (CR 601.2d) keeps them
     *       in {@code StackEntry.damageAssignments}, so the {@code targetId} handed to validation is
     *       null by design and the caller — not the predicate — decides whether that is legal. This
     *       used to be bought by declaring {@code PLAYER_OR_PERMANENT}, a category the interpreter
     *       no-ops on, which hid what the effect really targets.</li>
     *   <li>A predicate that accepts every player <em>and</em> every permanent restricts nothing, so
     *       there is nothing for it to demand.</li>
     * </ul>
     *
     * <p>Every other shape either narrows the domain ("a permanent, not a player") or narrows within
     * it, and can only judge a target that exists.</p>
     */
    private static boolean demandsPermanentTarget(TargetPredicate predicate, PermanentPredicate restriction,
                                                  CardEffect effect) {
        if (EffectResolution.distributesAmountsAmongTargets(effect)) {
            return false;
        }
        return !predicate.admits(TargetPredicate.Kind.PLAYER)
                || !(restriction instanceof PermanentTruePredicate);
    }

    /**
     * Why {@code target} is not legal under {@code restriction}. A conjunction blames the first
     * component that actually failed, so "target artifact creature" still reports "must be a
     * creature" for a land while a bespoke narrowing keeps the generic wording.
     */
    private String rejectionMessage(TargetPredicate predicate, PermanentPredicate restriction,
                                    Permanent target, FilterContext filterContext) {
        if (restriction instanceof PermanentAllOfPredicate allOf) {
            for (PermanentPredicate part : allOf.predicates()) {
                if (!predicateEvaluationService.matchesPermanentPredicate(target, part, filterContext)) {
                    return rejectionMessage(predicate, part, target, filterContext);
                }
            }
        }
        List<String> kinds = new ArrayList<>(describe(restriction));
        if (kinds.isEmpty()) {
            return "Target does not match the required predicate";
        }
        if (predicate.admits(TargetPredicate.Kind.PLAYER)) {
            kinds.add("player");
        }
        return "Target must be a " + joinKinds(kinds);
    }

    /**
     * The nouns {@code restriction} accepts, or empty when it has no natural phrasing — a
     * disjunction only phrases when every branch does, otherwise the sentence would understate
     * what is legal.
     */
    private static List<String> describe(PermanentPredicate restriction) {
        return switch (restriction) {
            case PermanentIsCreaturePredicate ignored -> List.of("creature");
            case PermanentIsLandPredicate ignored -> List.of("land");
            case PermanentIsPlaneswalkerPredicate ignored -> List.of("planeswalker");
            case PermanentIsBattlePredicate ignored -> List.of("battle");
            case PermanentAnyOfPredicate anyOf -> {
                List<String> kinds = new ArrayList<>();
                for (PermanentPredicate branch : anyOf.predicates()) {
                    List<String> branchKinds = describe(branch);
                    if (branchKinds.isEmpty()) {
                        yield List.of();
                    }
                    kinds.addAll(branchKinds);
                }
                yield List.copyOf(kinds);
            }
            default -> List.of();
        };
    }

    private static String joinKinds(List<String> kinds) {
        if (kinds.size() == 1) {
            return kinds.getFirst();
        }
        if (kinds.size() == 2) {
            return kinds.getFirst() + " or " + kinds.getLast();
        }
        return String.join(", ", kinds.subList(0, kinds.size() - 1)) + ", or " + kinds.getLast();
    }

    /**
     * The context a spec's narrowing predicate is evaluated in. Source-relative predicates
     * ("target creature blocking or blocked by this creature", "another creature you control")
     * resolve nothing without the source, so the ability's source card and — when the source is a
     * permanent — its controller must travel with the predicate.
     */
    private FilterContext sourceFilterContext(TargetValidationContext ctx) {
        FilterContext filterContext = FilterContext.of(ctx.gameData());
        if (ctx.sourceCard() != null) {
            filterContext = filterContext.withSourceCardId(ctx.sourceCard().getId());
            UUID controllerId = findSourcePermanentController(ctx);
            if (controllerId != null) {
                filterContext = filterContext.withSourceControllerId(controllerId);
            }
        }
        return filterContext;
    }

    public void validateEffectTargets(List<CardEffect> effects, TargetValidationContext context) {
        checkEffectTargets(effects, context)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    public void requireTarget(TargetValidationContext ctx) {
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Ability requires a target");
        }
    }

    public Permanent requireBattlefieldTarget(TargetValidationContext ctx) {
        requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetId());
        if (target == null) {
            throw new IllegalStateException("Invalid target permanent");
        }
        return target;
    }

    public void requireCreature(TargetValidationContext ctx, Permanent target) {
        if (!gameQueryService.isCreature(ctx.gameData(), target)) {
            throw new IllegalStateException("Target must be a creature");
        }
    }

    public void checkProtection(TargetValidationContext ctx, Permanent target) {
        if (hasProtectionFromSourceController(ctx, target)) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from the source's controller");
        }
        for (CardColor effectiveColor : gameQueryService.getEffectiveCardColors(ctx.gameData(), ctx.sourceCard())) {
            if (gameQueryService.hasProtectionFrom(ctx.gameData(), target, effectiveColor)) {
                throw new IllegalStateException(target.getCard().getName() + " has protection from "
                        + effectiveColor.name().toLowerCase());
            }
        }
        if (gameQueryService.hasProtectionFromSourceCardTypes(ctx.gameData(), target, ctx.sourceCard())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + ctx.sourceCard().getType().getDisplayName().toLowerCase() + "s");
        }
        if (gameQueryService.hasProtectionFromSourceSubtypes(target, ctx.sourceCard())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from source's subtype");
        }
    }

    private boolean hasProtectionFromSourceController(TargetValidationContext ctx, Permanent target) {
        UUID sourceControllerId = findSourcePermanentController(ctx);
        return gameQueryService.hasProtectionFromOpponents(ctx.gameData(), target, sourceControllerId);
    }

    public void requireTargetPlayer(TargetValidationContext ctx) {
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Ability requires a target player");
        }
        if (!ctx.gameData().playerIds.contains(ctx.targetId())) {
            throw new IllegalStateException("Target must be a player");
        }
    }

    public int findSourcePermanentIndex(TargetValidationContext ctx) {
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            List<Permanent> battlefield = ctx.gameData().playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (int i = 0; i < battlefield.size(); i++) {
                if (battlefield.get(i).getCard() == ctx.sourceCard()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public UUID findSourcePermanentController(TargetValidationContext ctx) {
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            List<Permanent> battlefield = ctx.gameData().playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getCard() == ctx.sourceCard()) {
                    return playerId;
                }
            }
        }
        return null;
    }
}
