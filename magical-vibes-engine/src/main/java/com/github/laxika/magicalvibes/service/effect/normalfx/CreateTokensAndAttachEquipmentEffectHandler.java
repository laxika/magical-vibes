package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachEquipmentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensAndAttachEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final CreateTokensAndAttachEquipmentSupport attachmentSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAndAttachEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createEffect = (CreateTokensAndAttachEquipmentEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, createEffect.token());
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        List<UUID> createdIds = new ArrayList<>(entry.getCreatedPermanentIds()
                .subList(createdBefore, entry.getCreatedPermanentIds().size()));
        if (!createdIds.isEmpty()) {
            attachmentSupport.begin(gameData, entry.getCard(), entry.getControllerId(), createdIds);
        }
    }
}
