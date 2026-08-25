package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpectersShriekEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpectersShriekEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpectersShriekEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInteractionSupport.resolveHandRevealAndChoose(
                gameData, entry, 1, List.of(CardType.LAND), List.of(), null,
                false, true, null, true, false);

        PendingInteraction.RevealedHandChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        if (choice != null) {
            gameData.interaction.replaceActiveInteraction(new PendingInteraction.SpectersShriekChoice(
                    choice.choosingPlayerId(), choice.targetPlayerId(), choice.validIndices(), choice.prompt()));
        }
    }
}
