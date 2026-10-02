package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachAurasEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Liberated Livestock's token creation and per-token Aura choices. */
@Component
public class CreateTokensAndAttachAurasEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    public CreateTokensAndAttachAurasEffectHandler(PermanentControlSupport permanentControlSupport) {
        this.permanentControlSupport = permanentControlSupport;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAndAttachAurasEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensAndAttachAurasEffect createEffect = (CreateTokensAndAttachAurasEffect) effect;
        List<UUID> createdIds = new ArrayList<>();
        for (CreateTokenEffect tokenEffect : createEffect.tokenEffects()) {
            createdIds.addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode()));
        }
        entry.getCreatedPermanentIds().addAll(createdIds);

        List<CardEffect> followUps = createdIds.stream()
                .map(id -> (CardEffect) new AttachAurasToSourceEffect(false, false, 1, false, id))
                .toList();
        if (!followUps.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
        }
    }
}
