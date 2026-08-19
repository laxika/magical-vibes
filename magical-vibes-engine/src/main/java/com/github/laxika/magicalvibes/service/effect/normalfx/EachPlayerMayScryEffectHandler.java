package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Resolves the per-player choice flow for {@link EachPlayerMayScryEffect}. */
@Component
public class EachPlayerMayScryEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayScryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayScryEffect scryEffect = (EachPlayerMayScryEffect) effect;
        List<UUID> players = scryEffect.remainingPlayerIds().isEmpty()
                ? (scryEffect.opponentsOnly()
                        ? apnapOpponents(gameData, entry.getControllerId())
                        : apnapPlayers(gameData))
                : scryEffect.remainingPlayerIds();
        if (!players.isEmpty()) {
            promptNext(gameData, entry.getCard(),
                    new EachPlayerMayScryEffect(scryEffect.count(), players, scryEffect.opponentsOnly()));
        }
    }

    /** Queues the next player's accept/decline choice. */
    public void promptNext(GameData gameData, Card sourceCard,
                           EachPlayerMayScryEffect effect) {
        UUID playerId = effect.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                playerId,
                List.of(effect),
                sourceCard.getName() + " — You may scry " + effect.count() + "."));
    }

    /** Returns every player in active-player-first order. */
    public static List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            return rotated;
        }
        return ordered;
    }

    public static List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        return apnapPlayers(gameData).stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
