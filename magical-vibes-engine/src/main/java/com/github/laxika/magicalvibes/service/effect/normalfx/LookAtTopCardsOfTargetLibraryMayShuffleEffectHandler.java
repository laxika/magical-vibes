package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayShuffleEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Visions: the controller looks at the top N cards of target player's library (they stay on
 * top in the same order), then may have that player shuffle their library. The looked-at card names
 * are revealed to the controller through the may-ability prompt; the optional shuffle reuses the
 * shared {@link ShuffleLibraryEffect} may-handler (targeting the same player).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsOfTargetLibraryMayShuffleEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsOfTargetLibraryMayShuffleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsOfTargetLibraryMayShuffleEffect e = (LookAtTopCardsOfTargetLibraryMayShuffleEffect) effect;

        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId() != null ? entry.getTargetId() : controllerId;
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        int actual = deck != null ? Math.min(e.count(), deck.size()) : 0;
        if (actual == 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + ": " + targetName + "'s library is empty."));
            return;
        }

        // The cards are only looked at (they stay on top in the same order); their names are shown
        // privately to the controller through the may-ability prompt below.
        String names = deck.subList(0, actual).stream().map(Card::getName).collect(Collectors.joining(", "));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                controllerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library."));
        log.info("Game {} - {} looks at top {} of {}'s library ({})", gameData.id, controllerName, actual, targetName, sourceName);

        String prompt = sourceName + " — Top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName
                + "'s library: " + names + ". Have that player shuffle their library?";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new ShuffleLibraryEffect(true)),
                prompt,
                targetPlayerId));
    }
}
