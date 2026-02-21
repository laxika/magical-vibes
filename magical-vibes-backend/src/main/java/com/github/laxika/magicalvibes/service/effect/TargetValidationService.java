package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAuraFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

        registry.register(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
            checkProtection(ctx, target);
        });

        registry.register(DealDamageToAnyTargetEffect.class, (ctx, effect) -> {
            requireTarget(ctx);
            if (ctx.gameData().playerIds.contains(ctx.targetPermanentId())) {
                return;
            }
            Permanent target = requireBattlefieldTarget(ctx);
            boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                    || target.getCard().getType() == CardType.PLANESWALKER;
            if (!validPermanentType) {
                throw new IllegalStateException("Target must be a creature, planeswalker, or player");
            }
            checkProtection(ctx, target);
        });

        registry.register(TapOrUntapTargetPermanentEffect.class, (ctx, effect) -> {
            requireBattlefieldTarget(ctx);
        });

        registry.register(TapTargetPermanentEffect.class, (ctx, effect) -> {
            requireBattlefieldTarget(ctx);
        });

        registry.register(UntapTargetPermanentEffect.class, (ctx, effect) -> {
            requireBattlefieldTarget(ctx);
        });

        registry.register(MillTargetPlayerEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(DealDamageToTargetPlayerEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(DealDamageToTargetPlayerByHandSizeEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(SacrificeCreatureEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(TargetPlayerGainsControlOfSourceCreatureEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(RevealTopCardOfLibraryEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(TargetPlayerGainsLifeEffect.class, (ctx, effect) -> {
            requireTargetPlayer(ctx);
        });

        registry.register(GainControlOfEnchantedTargetEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(GainControlOfTargetCreatureUntilEndOfTurnEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(ReturnAuraFromGraveyardToBattlefieldEffect.class, (ctx, effect) -> {
            if (ctx.targetZone() != Zone.GRAVEYARD) {
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

        registry.register(ReturnCreatureFromGraveyardToHandEffect.class, (ctx, effect) -> {
            if (ctx.targetZone() != Zone.GRAVEYARD) {
                throw new IllegalStateException("Spell requires a graveyard target");
            }
            if (ctx.targetPermanentId() == null) {
                throw new IllegalStateException("Spell requires a target creature card");
            }
            Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetPermanentId());
            if (graveyardCard == null) {
                throw new IllegalStateException("Target card not found in any graveyard");
            }
            if (graveyardCard.getType() != CardType.CREATURE) {
                throw new IllegalStateException("Target card must be a creature");
            }
        });

        registry.register(ReturnCardFromGraveyardToHandEffect.class, (ctx, effect) -> {
            if (ctx.targetZone() != Zone.GRAVEYARD) {
                throw new IllegalStateException("Spell requires a graveyard target");
            }
            if (ctx.targetPermanentId() == null) {
                throw new IllegalStateException("Spell requires a target card");
            }
            Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetPermanentId());
            if (graveyardCard == null) {
                throw new IllegalStateException("Target card not found in any graveyard");
            }
        });

        registry.register(PutTargetOnBottomOfLibraryEffect.class, (ctx, effect) -> {
            requireTarget(ctx);
            Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
            if (target == null || !gameQueryService.isCreature(ctx.gameData(), target)) {
                throw new IllegalStateException("Target must be a creature");
            }
        });

        registry.register(BoostTargetCreatureEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(BoostEnchantedCreatureEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(BoostEnchantedCreaturePerControlledSubtypeEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(DestroyCreatureBlockingThisEffect.class, (ctx, effect) -> {
            requireTarget(ctx);
            Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
            if (target == null || !gameQueryService.isCreature(ctx.gameData(), target) || !target.isBlocking()) {
                throw new IllegalStateException("Target must be a creature blocking this creature");
            }
            int sourceIndex = findSourcePermanentIndex(ctx);
            if (sourceIndex < 0 || !target.getBlockingTargets().contains(sourceIndex)) {
                throw new IllegalStateException("Target must be a creature blocking this creature");
            }
        });

        registry.register(DestroyTargetPermanentEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            checkProtection(ctx, target);
        });
        registry.register(TargetCreatureCantBlockThisTurnEffect.class, (ctx, effect) -> {
            Permanent target = requireBattlefieldTarget(ctx);
            requireCreature(ctx, target);
        });

        registry.register(ReturnTargetPermanentToHandEffect.class, (ctx, effect) -> {
            requireBattlefieldTarget(ctx);
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

    private int findSourcePermanentIndex(TargetValidationContext ctx) {
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
}


