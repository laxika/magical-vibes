package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Creates last-known copies of Three Dog's sacrificed Aura and attaches them to other attackers. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final AuraAttachmentService auraAttachmentService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent sacrificedAura = entry.getSacrificedPermanentSnapshot();
        if (sacrificedAura == null || !sacrificedAura.getCard().isAura()) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        UUID sourceId = sourcePermanentId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        List<Permanent> otherAttackingCreatures = List.copyOf(battlefield).stream()
                .filter(Permanent::isAttacking)
                .filter(permanent -> !permanent.getId().equals(sourceId))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> auraAttachmentService.canEnchant(
                        gameData, sacrificedAura.getCard(), entry.getControllerId(), permanent))
                .toList();

        for (Permanent creature : otherAttackingCreatures) {
            List<UUID> createdIds = tokenCopySupport.createTokenCopies(
                    gameData,
                    entry,
                    List.of(sacrificedAura.getCard()),
                    null,
                    entry.getControllerId(),
                    new CreateTokenCopyOfTargetPermanentEffect());
            for (UUID tokenId : createdIds) {
                attachToken(gameData, entry, tokenId, creature);
            }
        }
    }

    private void attachToken(GameData gameData, StackEntry entry, UUID tokenId, Permanent creature) {
        Permanent token = gameQueryService.findPermanentById(gameData, tokenId);
        if (token == null || !auraAttachmentService.canEnchant(
                gameData, token.getCard(), entry.getControllerId(), creature)) {
            return;
        }
        gameData.expireFloatingEffectsForUnattachedSource(token.getId());
        token.setAttachedTo(creature.getId());
        token.setTimestamp(gameData.nextTimestamp());
        triggerCollectionService.checkAuraAttachedTriggers(gameData, token, creature.getId());
    }
}
