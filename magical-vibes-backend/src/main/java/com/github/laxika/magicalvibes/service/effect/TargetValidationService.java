package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TargetValidationService {

    private final GameQueryService gameQueryService;
    private final TargetValidatorRegistry registry;

    public TargetValidationService(GameQueryService gameQueryService, TargetValidatorRegistry registry) {
        this.gameQueryService = gameQueryService;
        this.registry = registry;
    }

    public void validateEffectTargets(List<CardEffect> effects, TargetValidationContext context) {
        for (CardEffect effect : effects) {
            TargetValidator validator = registry.getValidator(effect);
            if (validator != null) {
                validator.validate(context, effect);
            }
        }
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
