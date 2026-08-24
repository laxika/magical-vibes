package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BindingNegotiationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BindingNegotiationEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BindingNegotiationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveHandRevealAndChoose(
                gameData, entry, 1, List.of(CardType.LAND), List.of(), null,
                true, false, null, true, false, 0);

        PendingInteraction.RevealedHandChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        if (choice != null) {
            gameData.interaction.replaceActiveInteraction(choice.withDeclineEffect(
                    new PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect()));
            return;
        }

        insertFallbackEffect(entry, effect);
    }

    private void insertFallbackEffect(StackEntry entry, CardEffect effect) {
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Binding Negotiation effect is not in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect()));
    }
}
