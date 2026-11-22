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
        // One instruction creates all of its tokens in one event: it puts the specified tokens onto the battlefield (CR 701.7a).
        List<CreateTokenEffect> tokenEffects = createEffect.tokenEffects();
        if (tokenEffects.isEmpty()) {
            return;
        }
        CreateTokenEffect together = tokenEffects.getFirst()
                .withSimultaneousTokens(tokenEffects.subList(1, tokenEffects.size()));
        List<UUID> createdIds = new ArrayList<>(permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), together, entry.getCard().getSetCode()));
        entry.getCreatedPermanentIds().addAll(createdIds);

        List<CardEffect> followUps = createdIds.stream()
                .map(id -> (CardEffect) new AttachAurasToSourceEffect(false, false, 1, false, id))
                .toList();
        if (!followUps.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
        }
    }
}
