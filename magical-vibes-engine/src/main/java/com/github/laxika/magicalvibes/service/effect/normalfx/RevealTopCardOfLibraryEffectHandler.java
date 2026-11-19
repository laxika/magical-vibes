package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reveals the top card of the library of the player {@link SubjectResolutionService} finds for the effect's subject
 * (the controller's own when it names nobody), with Prophecy's land life-gain rider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardOfLibraryEffect revealEffect = (RevealTopCardOfLibraryEffect) effect;

        UUID deckOwnerId = subjectResolutionService.player(gameData, entry, effect, revealEffect.player());
        if (deckOwnerId == null) {
            deckOwnerId = entry.getControllerId();
        }
        List<Card> deck = gameData.playerDecks.get(deckOwnerId);
        String playerName = gameData.playerIdToName.get(deckOwnerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
        } else {
            Card topCard = deck.getFirst();
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " reveals ", topCard, " from the top of their library."));

            if (revealEffect.lifeGainIfLand() > 0 && topCard.hasType(CardType.LAND)) {
                lifeSupport.applyGainLife(gameData, entry.getControllerId(), revealEffect.lifeGainIfLand(),
                        entry.getCard().getName(), entry.getCard(), entry.getEntryType());
            }
        }

        log.info("Game {} - {} reveals top card of library", gameData.id, playerName);
    }
}
