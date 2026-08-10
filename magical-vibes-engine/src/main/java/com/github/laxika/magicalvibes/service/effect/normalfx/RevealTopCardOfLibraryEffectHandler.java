package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardOfLibraryEffect revealEffect = (RevealTopCardOfLibraryEffect) effect;

        UUID deckOwnerId = resolveDeckOwner(entry, revealEffect.owner());
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

    /** Falls back to the controller when a target-player form resolves without a target. */
    private static UUID resolveDeckOwner(StackEntry entry, LibraryOwner owner) {
        if (owner != LibraryOwner.TARGET_PLAYER
                && owner != LibraryOwner.ENCHANTED_PERMANENT_CONTROLLER) {
            return entry.getControllerId();
        }
        return entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
    }
}
