package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyAttachmentsOnTargetCreatureEffectHandler implements NormalEffectHandlerBean {

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

        List<Permanent> attachmentsToDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (!targetId.equals(p.getAttachedTo())) {
                return;
            }
            boolean isAura = p.getCard().getSubtypes().contains(CardSubtype.AURA);
            boolean isEquipment = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
            if ((e.auras() && isAura) || (e.equipment() && isEquipment)) {
                attachmentsToDestroy.add(p);
            }
        });

        for (Permanent attachment : attachmentsToDestroy) {
            destructionSupport.tryDestroyAndLog(gameData, attachment, entry.getCard().getName());
        }
    }
}
