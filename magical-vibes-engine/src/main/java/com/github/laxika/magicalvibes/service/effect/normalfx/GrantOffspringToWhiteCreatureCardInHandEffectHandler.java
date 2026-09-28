package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantOffspringToWhiteCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Begins Cottontail Caretaker's resolution-time hand choice. */
@Component
@RequiredArgsConstructor
public class GrantOffspringToWhiteCreatureCardInHandEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantOffspringToWhiteCreatureCardInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantOffspringToWhiteCreatureCardInHandEffect offspringEffect =
                (GrantOffspringToWhiteCreatureCardInHandEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.CREATURE) && card.getColors().contains(CardColor.WHITE)) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualOffspringCardChoice(
                entry.getControllerId(), validIndices,
                "Choose a white creature card in your hand to perpetually give offspring "
                        + offspringEffect.offspringCost() + ".",
                offspringEffect.offspringCost()));
    }
}
