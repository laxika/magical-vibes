package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.MayCastRippleCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Shared state and prompts for Ripple's revealed-card free casts. */
@Component
@RequiredArgsConstructor
public class RippleFreeCastSupport {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void offerOrBottom(GameData gameData, UUID ownerId, UUID casterId, String cardName,
                              List<Card> heldCards) {
        List<Card> castable = heldCards.stream()
                .filter(card -> cardName.equals(card.getName()) && !card.hasType(CardType.LAND))
                .toList();
        if (castable.isEmpty()) {
            beginBottomReorder(gameData, ownerId, heldCards);
            return;
        }

        gameData.queueInteraction(new PendingInteraction.RippleFreeCastGroup(
                ownerId, casterId, cardName, new ArrayList<>(heldCards)));
        for (int i = castable.size() - 1; i >= 0; i--) {
            Card card = castable.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    casterId,
                    List.of(new MayCastRippleCardWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
    }

    public boolean hasPendingOffers(GameData gameData) {
        return gameData.pendingMayAbilities.stream()
                .anyMatch(ability -> ability.effects().stream()
                        .anyMatch(effect -> effect instanceof MayCastRippleCardWithoutPayingManaCostEffect));
    }

    public void clearPendingOffers(GameData gameData) {
        gameData.pendingMayAbilities.removeIf(ability -> ability.effects().stream()
                .anyMatch(effect -> effect instanceof MayCastRippleCardWithoutPayingManaCostEffect));
    }

    public void beginBottomReorder(GameData gameData, UUID ownerId, List<Card> cards) {
        if (cards.isEmpty()) {
            return;
        }
        List<Card> heldCards = new ArrayList<>(cards);
        if (heldCards.size() == 1) {
            gameData.playerDecks.get(ownerId).add(heldCards.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                ownerId,
                heldCards,
                true,
                ownerId,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
    }
}
