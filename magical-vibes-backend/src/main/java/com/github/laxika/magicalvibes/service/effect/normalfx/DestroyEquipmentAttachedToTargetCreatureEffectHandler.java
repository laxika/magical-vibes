package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentAttachedToTargetCreatureEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyEquipmentAttachedToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEquipmentAttachedToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
                if (targetId == null) {
                    return;
                }

                List<Permanent> equipmentToDestroy = new ArrayList<>();
                gameData.forEachPermanent((playerId, p) -> {
                    if (targetId.equals(p.getAttachedTo())
                            && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        equipmentToDestroy.add(p);
                    }
                });

                for (Permanent equipment : equipmentToDestroy) {
                    destructionSupport.tryDestroyAndLog(gameData, equipment, entry.getCard().getName());
                }
    }
}
