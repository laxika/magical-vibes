package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Destroys the Auras and/or Equipment attached to the entry's target permanent, all at once. Once that permanent has
 * left the battlefield (a non-targeting "that creature" gone before Corrosive Ooze's delayed ability resolves), the ones
 * attached to it just before it left are destroyed (CR 608.2h), from the entry's record of them
 * ({@link StackEntry#lastKnownPermanentAttachments}).
 */
@Component
@RequiredArgsConstructor
public class DestroyAttachmentsOnTargetCreatureEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAttachmentsOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyAttachmentsOnTargetCreatureEffect e = (DestroyAttachmentsOnTargetCreatureEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        // What is attached to it now, and, once it has left the battlefield (destroyed by the same spell, Eaten by
        // Spiders, or gone before a delayed ability resolves, Corrosive Ooze), what was attached to it just before.
        DepartedAttachments departed = entry.lastKnownPermanentAttachments(targetId);
        List<Permanent> attachmentsToDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            boolean attached = targetId.equals(p.getAttachedTo());
            boolean isAura = attached ? p.getCard().getSubtypes().contains(CardSubtype.AURA)
                    : departed.auraCardIds().contains(p.getCard().getId());
            boolean isEquipment = attached ? p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    : departed.equipmentPermanentIds().contains(p.getId());
            if ((e.auras() && isAura) || (e.equipment() && isEquipment)) {
                attachmentsToDestroy.add(p);
            }
        });
        if (!attachmentsToDestroy.isEmpty()) {
            destructionSupport.destroyBatch(gameData, attachmentsToDestroy, entry.getCard().getName(), false);
        }
    }
}
