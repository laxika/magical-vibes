package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachEquipmentToCreatedTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link AttachEquipmentToCreatedTokensEffect}: offers an Equipment for each token the creation made, from the
 * step's {@code createdFrom}-th created permanent on, through {@link CreateTokensAndAttachEquipmentSupport}.
 */
@Component
@RequiredArgsConstructor
public class AttachEquipmentToCreatedTokensEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokensAndAttachEquipmentSupport attachmentSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachEquipmentToCreatedTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attach = (AttachEquipmentToCreatedTokensEffect) effect;
        List<UUID> created = entry.getCreatedPermanentIds();
        if (attach.createdFrom() >= created.size()) {
            return;
        }
        attachmentSupport.begin(gameData, entry.getCard(), entry.getControllerId(),
                List.copyOf(created.subList(attach.createdFrom(), created.size())));
    }
}
