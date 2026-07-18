package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RebirthAnteEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RebirthAnteEffect} (Rebirth): "Each player may ante the top card of their library.
 * If a player does, that player's life total becomes 20."
 *
 * <p>Seeds one accept/decline {@link PendingMayAbility} per player in APNAP order (active player
 * first), each deciding player being the potential anteing player. The accept branch lives in
 * {@code RebirthAnteHandler}. A player with an empty library has no top card to ante and is skipped.
 */
@Component
@RequiredArgsConstructor
public class RebirthAnteEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RebirthAnteEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // APNAP order: active player first, then the others in turn order.
        List<UUID> players = new ArrayList<>();
        players.add(gameData.activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                players.add(playerId);
            }
        }

        for (UUID playerId : players) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                // No top card to ante — this player can't ante, so nothing happens for them.
                continue;
            }
            String prompt = "Ante the top card of your library? If you do, your life total becomes 20. ("
                    + entry.getCard().getName() + ")";
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    entry.getCard(), playerId, List.of(effect), prompt));
        }
    }
}
