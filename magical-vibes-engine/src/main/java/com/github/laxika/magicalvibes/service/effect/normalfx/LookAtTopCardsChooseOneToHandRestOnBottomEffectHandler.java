package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsChooseOneToHandRestOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsChooseOneToHandRestOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsChooseOneToHandRestOnBottomEffect e = (LookAtTopCardsChooseOneToHandRestOnBottomEffect) effect;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        // Fewer than two cards seen: the single card (if any) simply goes to hand, nothing on bottom.
        if (topCards.size() <= 1) {
            for (Card card : topCards) {
                gameData.addCardToHand(controllerId, card);
            }
            if (!topCards.isEmpty()) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " puts " + topCards.getFirst().getName() + " into their hand.");
            }
            return;
        }

        // Choose one card to put into hand; the rest go on the bottom of the library.
        List<UUID> cardIds = topCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, topCards, cardIds,
                false, true, true, false, 0, null, 1,
                "Look at the top " + topCards.size() + " cards of your library. Put one into your hand"
                        + " and the rest on the bottom of your library."));
    }
}
