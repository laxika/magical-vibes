package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.model.effect.UnexpectedResultsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnexpectedResultsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnexpectedResultsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " shuffles their library (" + sourceName + ")."));

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").").build());
        log.info("Game {} - {} reveals top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        if (topCard.hasType(CardType.LAND)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    topCard,
                    controllerId,
                    List.of(effect),
                    sourceName + " — Put " + topCard.getName() + " onto the battlefield and return " + sourceName + " to your hand?",
                    entry.getCard().getId()
            ));
            return;
        }

        // Nonland: the shared "reveal top, may play it for free" flow already casts without paying
        // and leaves an unplayed card on top of the library, which is exactly this card's wording.
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(new RevealTopCardMayPlayFreeEffect(LookDestination.TOP_OF_LIBRARY)),
                sourceName + " — Cast " + topCard.getName() + " without paying its mana cost?"
        ));
    }
}
