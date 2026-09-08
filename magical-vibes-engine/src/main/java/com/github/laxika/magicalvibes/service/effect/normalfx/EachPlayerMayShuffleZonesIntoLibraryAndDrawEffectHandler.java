package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the per-player choices for a hand-and-graveyard shuffle-and-draw effect. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayShuffleZonesIntoLibraryAndDrawEffectHandler implements NormalEffectHandlerBean {

    private final ZoneToLibraryService zoneToLibraryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect shuffleEffect =
                (EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect) effect;
        List<UUID> players = shuffleEffect.remainingPlayerIds().isEmpty()
                ? apnapPlayers(gameData)
                : shuffleEffect.remainingPlayerIds();
        if (!players.isEmpty()) {
            promptNext(gameData, entry.getCard(), new EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(
                    shuffleEffect.cardsToDraw(), players, shuffleEffect.acceptedPlayerIds()));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                sourceCard.getName() + " — You may shuffle your hand and graveyard into your library."
        ));
    }

    public void resolveAcceptedPlayers(GameData gameData, List<UUID> acceptedPlayerIds, int cardsToDraw) {
        for (UUID playerId : acceptedPlayerIds) {
            zoneToLibraryService.moveHandAndGraveyardIntoLibrary(gameData, playerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }
        for (UUID playerId : acceptedPlayerIds) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, cardsToDraw);
        }
    }

    private static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
            rotated.addAll(players.subList(0, activeIndex));
            return rotated;
        }
        return players;
    }
}
