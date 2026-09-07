package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the per-player choices for a discard-hand-and-draw effect. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardHandThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardHandThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayDiscardHandThenDrawEffect discardEffect =
                (EachPlayerMayDiscardHandThenDrawEffect) effect;
        List<UUID> players = discardEffect.remainingPlayerIds().isEmpty()
                ? apnapPlayers(gameData)
                : discardEffect.remainingPlayerIds();
        if (!players.isEmpty()) {
            UUID sourceControllerId = discardEffect.sourceControllerId() != null
                    ? discardEffect.sourceControllerId() : entry.getControllerId();
            promptNext(gameData, entry.getCard(), new EachPlayerMayDiscardHandThenDrawEffect(
                    discardEffect.cardsToDraw(), sourceControllerId,
                    players, discardEffect.acceptedPlayerIds()));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           EachPlayerMayDiscardHandThenDrawEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                sourceCard.getName() + " - You may discard your hand and draw "
                        + effect.cardsToDraw() + " cards."));
    }

    public void resolveAcceptedPlayers(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                       List<UUID> acceptedPlayerIds, int cardsToDraw) {
        for (UUID playerId : acceptedPlayerIds) {
            discardHandEffectHandler.discardHand(gameData, playerId, sourceControllerId,
                    sourceCard.getName());
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
