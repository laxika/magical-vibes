package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyReduceCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseCardFromHandToPerpetuallyReduceCastCostEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandToPerpetuallyReduceCastCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseCardFromHandToPerpetuallyReduceCastCostEffect reductionEffect =
                (ChooseCardFromHandToPerpetuallyReduceCastCostEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (!hand.get(i).hasType(CardType.LAND)) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualCastCostHandCardChoice(
                entry.getControllerId(), validIndices, reductionEffect.amount(),
                "Choose a nonland card from your hand."));
    }

    public static void remember(GameData gameData, Card card, int amount) {
        PerpetualCardCastCostSupport.remember(gameData, card, amount);
    }
}
