package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Invoke Calamity's single hand-and-graveyard spell selection. */
@Component
@RequiredArgsConstructor
public class CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    private static final int MAX_TOTAL_MANA_VALUE = 6;

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> eligible = new ArrayList<>();
        addEligible(eligible, gameData.playerHands.get(controllerId), true);
        addEligible(eligible, gameData.playerGraveyards.get(controllerId), false);
        if (eligible.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.InvokeCalamityCastChoice(
                        controllerId, eligible.stream().map(Card::getId).toList()));
    }

    private static void addEligible(List<Card> eligible, List<Card> cards, boolean fromHand) {
        if (cards == null) {
            return;
        }
        for (Card card : cards) {
            if (isEligible(card) && (!fromHand || !card.isCastOnlyFromGraveyard())) {
                eligible.add(card);
            }
        }
    }

    static boolean isEligible(Card card) {
        return (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && card.getManaValue() <= MAX_TOTAL_MANA_VALUE;
    }
}
