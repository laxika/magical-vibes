package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopTenCardsForColorPairsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Niv-Mizzet's top-ten color-pair selection. */
@Component
@RequiredArgsConstructor
public class RevealTopTenCardsForColorPairsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final NivMizzetRevealSupport nivMizzetRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopTenCardsForColorPairsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, 10);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> revealedCards = result.topCards();
        GameLog.Builder revealLog = GameLog.builder()
                .text(result.playerName() + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                revealLog.text(", ");
            }
            revealLog.card(revealedCards.get(i));
        }
        gameLogService.append(gameData,
                revealLog.text(" with ").card(entry.getCard()).text(".").build());

        nivMizzetRevealSupport.begin(gameData, controllerId, revealedCards);
    }
}
