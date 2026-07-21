package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cascade (CR 702.85): "When you cast this spell, exile cards from the top of your library until you
 * exile a nonland card that costs less. You may cast it without paying its mana cost. Put the exiled
 * cards on the bottom of your library in a random order."
 *
 * <p>Digs the caster's library one card at a time until it exiles a nonland card whose mana value is
 * strictly less than the cascade spell's mana value ({@code entry.getCard().getManaValue()}), then
 * reuses the {@link LibrarySearchDestination#CAST_WITHOUT_PAYING} flow (shared with Sunbird's
 * Invocation) to let the caster cast that single card without paying its mana cost and put the rest
 * on the bottom in a random order. If no such card is found, all exiled cards go to the bottom in a
 * random order.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CascadeEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CascadeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        int threshold = entry.getCard().getManaValue();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(sourceName + " (Cascade): " + playerName + "'s library is empty."));
            return;
        }

        // Exile from the top until a nonland card with mana value < the cascade spell's mana value.
        List<Card> exiled = new ArrayList<>();
        Card hit = null;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            exiled.add(top);
            if (!top.hasType(CardType.LAND) && top.getManaValue() < threshold) {
                hit = top;
                break;
            }
        }

        log.info("Game {} - {} Cascade from {} (MV {}): exiled {} cards, hit={}",
                gameData.id, playerName, sourceName, threshold, exiled.size(),
                hit != null ? hit.getName() : "none");

        if (hit == null) {
            // Dug through the whole library without a qualifying card — bottom everything randomly.
            Collections.shuffle(exiled);
            deck.addAll(exiled);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName
                    + " (Cascade): no nonland card with lesser mana value found. Exiled cards go to the"
                    + " bottom of the library in a random order."));
            return;
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .text(playerName + " cascades into ").card(hit).text(" (" + sourceName + ").").build());

        // Reuse the shared cast-without-paying flow: cast the hit card for free (or decline), then put
        // the other exiled cards on the bottom of the library in a random order.
        String prompt = "Cascade — you may cast " + hit.getName() + " without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, List.of(hit))
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(exiled))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
    }
}
