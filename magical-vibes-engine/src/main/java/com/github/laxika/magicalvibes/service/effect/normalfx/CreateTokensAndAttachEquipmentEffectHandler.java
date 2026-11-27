package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachEquipmentToCreatedTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachEquipmentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the effect's tokens, then offers an Equipment for each of them through an inserted
 * {@link AttachEquipmentToCreatedTokensEffect} step, which runs once the creation has finished: its creating player may
 * stop it to order its replacements (CR 616.1). A creation waiting for a first-creation replacement offer is resolved
 * again once answered, and inserts the step then.
 */
@Component
@RequiredArgsConstructor
public class CreateTokensAndAttachEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final AttachEquipmentToCreatedTokensEffectHandler attachHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAndAttachEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createEffect = (CreateTokensAndAttachEquipmentEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, createEffect.token());
        if (gameData.pendingTokenCreationReplacement != null) {
            return;
        }
        AttachEquipmentToCreatedTokensEffect attach = new AttachEquipmentToCreatedTokensEffect(createdBefore);
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(attach));
        } else if (!gameData.interaction.isAwaitingInput()) {
            attachHandler.resolve(gameData, entry, attach);
        }
    }
}
