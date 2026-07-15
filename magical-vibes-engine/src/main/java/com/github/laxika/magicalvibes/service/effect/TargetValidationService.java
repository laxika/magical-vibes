package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Service;

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
                if (spec.category() != TargetCategory.NONE) {
                    validateSpec(context, spec);
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
     * hand-written {@code @ValidatesTarget} validators perform. Called only when the spec's
     * category is not {@code NONE}. Semantics mirror the corresponding validators exactly (see
     * {@code DamageTargetValidators} / {@code DestructionTargetValidators}); predicate narrowing
     * and the harmful protection check apply to permanent targets only.
     */
    private void validateSpec(TargetValidationContext ctx, TargetSpec spec) {
        switch (spec.category()) {
            case CREATURE -> {
                Permanent target = requireBattlefieldTarget(ctx);
                requireCreature(ctx, target);
            }
            case CREATURE_OR_PLANESWALKER -> {
                Permanent target = requireBattlefieldTarget(ctx);
                boolean valid = gameQueryService.isCreature(ctx.gameData(), target)
                        || target.getCard().hasType(CardType.PLANESWALKER);
                if (!valid) {
                    throw new IllegalStateException("Target must be a creature or planeswalker");
                }
            }
            case ANY_TARGET -> {
                requireTarget(ctx);
                if (!ctx.gameData().playerIds.contains(ctx.targetId())) {
                    Permanent target = requireBattlefieldTarget(ctx);
                    boolean valid = gameQueryService.isCreature(ctx.gameData(), target)
                            || target.getCard().hasType(CardType.PLANESWALKER);
                    if (!valid) {
                        throw new IllegalStateException("Target must be a creature, planeswalker, or player");
                    }
                }
            }
            case PLAYER_OR_PLANESWALKER -> {
                requireTarget(ctx);
                if (!ctx.gameData().playerIds.contains(ctx.targetId())) {
                    Permanent target = requireBattlefieldTarget(ctx);
                    if (!target.getCard().hasType(CardType.PLANESWALKER)) {
                        throw new IllegalStateException("Target must be a player or planeswalker");
                    }
                }
            }
            case PERMANENT -> requireBattlefieldTarget(ctx);
            case LAND -> {
                Permanent target = requireBattlefieldTarget(ctx);
                if (!target.getCard().hasType(CardType.LAND)) {
                    throw new IllegalStateException("Target must be a land");
                }
            }
            // Player and zone categories perform NO permanent-type check here: players are
            // validated on the player path, and spell/graveyard/exile targets are guarded by
            // their own zone paths.
            case PLAYER, PLAYER_OR_PERMANENT, SPELL_ON_STACK, GRAVEYARD_CARD,
                 ANY_GRAVEYARD_CARD, EXILE_CARD, NONE -> { }
        }

        // Predicate narrowing and the harmful protection check apply to a permanent target only.
        Permanent target = ctx.targetId() == null
                ? null
                : gameQueryService.findPermanentById(ctx.gameData(), ctx.targetId());
        if (target == null) {
            return;
        }
        if (spec.predicate() != null
                && !predicateEvaluationService.matchesPermanentPredicate(ctx.gameData(), target, spec.predicate())) {
            throw new IllegalStateException("Target does not match the required predicate");
        }
        if (spec.harmful()) {
            checkProtection(ctx, target);
        }
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
        if (gameQueryService.hasProtectionFrom(ctx.gameData(), target, ctx.sourceCard().getColor())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + ctx.sourceCard().getColor().name().toLowerCase());
        }
        if (gameQueryService.hasProtectionFromSourceCardTypes(target, ctx.sourceCard())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + ctx.sourceCard().getType().getDisplayName().toLowerCase() + "s");
        }
        if (gameQueryService.hasProtectionFromSourceSubtypes(target, ctx.sourceCard())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from source's subtype");
        }
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
