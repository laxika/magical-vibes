package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsBottomThenDamageIfCopyRevealedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardsBottomThenDamageIfCopyRevealedEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsBottomThenDamageIfCopyRevealedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealTopCardsBottomThenDamageIfCopyRevealedEffect) effect;

        String cardName = entry.getCard().getName();
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return; // empty library — already logged

        String revealedNames = result.topCards().stream()
                .map(Card::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        gameBroadcastService.logAndBroadcast(gameData,
                result.playerName() + " reveals " + revealedNames + " from the top of their library with " + cardName + ".");

        boolean copyRevealed = result.topCards().stream().anyMatch(c -> c.getName().equals(cardName));

        // If a copy was revealed, the source deals its damage to the chosen any-target.
        UUID targetId = entry.getTargetId();
        if (copyRevealed && targetId != null) {
            damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, e.damage(), false);
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Put the revealed cards on the bottom of the library in any order.
        libraryRevealSupport.reorderRemainingToBottom(gameData, result.controllerId(), result.topCards());
    }
}
