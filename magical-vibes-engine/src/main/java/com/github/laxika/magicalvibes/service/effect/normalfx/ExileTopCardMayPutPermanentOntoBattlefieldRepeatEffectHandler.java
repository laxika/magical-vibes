package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Primal Surge. Runs the first iteration of the exile-and-may-put loop; each accepted put runs
 * another iteration from
 * {@link com.github.laxika.magicalvibes.service.interaction.ExiledPermanentPutOntoBattlefieldChoiceInteractionHandler}.
 */
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffectHandler implements NormalEffectHandlerBean {

    private final ExileTopCardMayPutPermanentSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        support.exileTopCardAndPromptIfPermanent(gameData, entry.getControllerId(), entry.getCard().getName());
    }
}
