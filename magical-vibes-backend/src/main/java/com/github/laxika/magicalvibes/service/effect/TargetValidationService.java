package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TargetValidationService {

    private final GameQueryService gameQueryService;
    private final TargetValidatorRegistry registry;

    public TargetValidationService(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
        this.registry = new TargetValidatorRegistry();

        registry.register(DealXDamageToTargetCreatureEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
            checkProtection(ctx, target);
        });

        registry.register(DealDamageToTargetCreatureEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
            checkProtection(ctx, target);
        });

        registry.register(TapTargetPermanentEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            TapTargetPermanentEffect tapEffect = (TapTargetPermanentEffect) effect;
            if (!tapEffect.allowedTypes().contains(target.getCard().getType())) {
                throw new IllegalStateException("Target must be an artifact, creature, or land");
            }
        });

        registry.register(MillTargetPlayerEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(SacrificeCreatureEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(RevealTopCardOfLibraryEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(GainControlOfEnchantedTargetEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(ReturnAuraFromGraveyardToBattlefieldEffect.class, (ctx, effect) -> {
            if (ctx.targetZone() != TargetZone.GRAVEYARD) {
                throw new IllegalStateException("Ability requires a graveyard target");
            }
            if (ctx.targetPermanentId() == null) {
                throw new IllegalStateException("Ability requires a target Aura card");
            }
            Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetPermanentId());
            if (graveyardCard == null) {
                throw new IllegalStateException("Target card not found in any graveyard");
            }
            if (!graveyardCard.isAura()) {
                throw new IllegalStateException("Target card must be an Aura");
            }
        });

        registry.register(PutTargetOnBottomOfLibraryEffect.class, (ctx, effect) -> {
            requireTarget(ctx);
            Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
            if (target == null || !gameQueryService.isCreature(ctx.gameData(), target)) {
                throw new IllegalStateException("Target must be a creature");
            }
        });

        registry.register(BoostTargetBlockingCreatureEffect.class, (ctx, effect) -> {
            requireTarget(ctx);
            Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
            if (target == null || !gameQueryService.isCreature(ctx.gameData(), target) || !target.isBlocking()) {
                throw new IllegalStateException("Target must be a blocking creature");
            }
        });

        registry.register(GainControlOfTargetAuraEffect.class, (ctx, effect) -> {
            requireTarget(ctx);
            Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
            if (target == null || target.getCard().getType() != CardType.ENCHANTMENT
                    || !target.getCard().getSubtypes().contains(CardSubtype.AURA)
                    || target.getAttachedTo() == null) {
                throw new IllegalStateException("Target must be an Aura attached to a permanent");
            }
        });
    }

    public void validateEffectTargets(List<CardEffect> effects, TargetValidationContext context) {
        for (CardEffect effect : effects) {
            TargetValidator validator = registry.getValidator(effect);
            if (validator != null) {
                validator.validate(context, effect);
            }
        }
    }

    private void requireTarget(TargetValidationContext ctx) {
        if (ctx.targetPermanentId() == null) {
            throw new IllegalStateException("Ability requires a target");
        }
    }

    private Permanent requireBattlefieldTarget(TargetValidationContext ctx) {
        requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
        if (target == null) {
            throw new IllegalStateException("Invalid target permanent");
        }
        return target;
    }

    private void requireCreature(TargetValidationContext ctx, Permanent target) {
        if (!gameQueryService.isCreature(ctx.gameData(), target)) {
            throw new IllegalStateException("Target must be a creature");
        }
    }

    private void checkProtection(TargetValidationContext ctx, Permanent target) {
        if (gameQueryService.hasProtectionFrom(ctx.gameData(), target, ctx.sourceCard().getColor())) {
            throw new IllegalStateException(target.getCard().getName() + " has protection from " + ctx.sourceCard().getColor().name().toLowerCase());
        }
    }

    private void requireTargetPlayer(TargetValidationContext ctx) {
        if (ctx.targetPermanentId() == null) {
            throw new IllegalStateException("Ability requires a target player");
        }
        if (!ctx.gameData().playerIds.contains(ctx.targetPermanentId())) {
            throw new IllegalStateException("Target must be a player");
        }
    }
}
