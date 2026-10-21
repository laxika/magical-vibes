package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSacrificedAuraAttachedToOtherAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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

        // The copy takes the Aura's copiable values (CR 707.2), not temporary effects such as a granted
        // artifact type, which the snapshot's card records as last-known information.
        List<Permanent> otherAttackingCreatures = List.copyOf(battlefield).stream()
                .filter(Permanent::isAttacking)
                .filter(permanent -> !permanent.getId().equals(sourceId))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> auraAttachmentService.canEnchant(
                        gameData, sacrificedAura.getCard(), entry.getControllerId(), permanent))
                .toList();

        for (Permanent creature : otherAttackingCreatures) {
            // The copy enters attached to this creature, so its controller isn't asked what it enchants.
            tokenCopySupport.createTokenCopiesAttachedTo(
                    gameData,
                    entry,
                    List.of(sacrificedAura.getOriginalCard()),
                    null,
                    entry.getControllerId(),
                    new CreateTokenCopyOfTargetPermanentEffect(),
                    creature);
        }
    }
}
