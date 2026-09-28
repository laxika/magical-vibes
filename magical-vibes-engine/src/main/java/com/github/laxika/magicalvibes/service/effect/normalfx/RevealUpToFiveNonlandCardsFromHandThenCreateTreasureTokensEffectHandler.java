package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUpToFiveNonlandCardsFromHandThenCreateTreasureTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> validCardIds = gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .limit(5)
                .map(Card::getId)
                .toList();

        if (validCardIds.isEmpty()) {
            entry.setEventValue(0);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.RevealAnyNumberOfCardsFromHandChoice(
                        controllerId, validCardIds, entry.getCard().getName(),
                        new PendingInteraction.DuplicateManaValueRevealContext(
                                CreateTokenEffect.ofTreasureToken(1))));
    }
}
