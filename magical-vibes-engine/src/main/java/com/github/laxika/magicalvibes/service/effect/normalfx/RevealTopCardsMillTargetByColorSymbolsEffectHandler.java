package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMillTargetByColorSymbolsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardsMillTargetByColorSymbolsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsMillTargetByColorSymbolsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTopCardsMillTargetByColorSymbolsEffect) effect;

        String cardName = entry.getCard().getName();
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return; // empty library — already logged

        String revealedNames = result.topCards().stream()
                .map(Card::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(result.playerName() + " reveals " + revealedNames + " from the top of their library with " + cardName + "."));

        int symbols = 0;
        for (Card card : result.topCards()) {
            ManaCost cost = card.getParsedManaCost();
            if (cost != null) {
                symbols += cost.countColorSymbols(e.color());
            }
        }

        // For each matching mana symbol, the target player mills a card.
        graveyardService.resolveMillPlayer(gameData, entry.getTargetId(), symbols);

        // Then put the revealed cards on the bottom of the controller's library in any order.
        libraryRevealSupport.reorderRemainingToBottom(gameData, result.controllerId(), result.topCards());
    }
}
