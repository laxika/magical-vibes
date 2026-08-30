package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardIfPlayerDealtDamageThisWayEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the discard rider for players that actually received damage earlier in this resolution.
 */
@Component
@RequiredArgsConstructor
public class DiscardIfPlayerDealtDamageThisWayEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardIfPlayerDealtDamageThisWayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> damagedPlayers = new ArrayList<>();
        for (UUID playerId : entry.getPlayersDealtDamageThisResolution()) {
            if (gameData.playerIds.contains(playerId)) {
                damagedPlayers.add(playerId);
            }
        }
        if (!damagedPlayers.isEmpty()) {
            playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                    DiscardFollowUp.eachPlayer(damagedPlayers, entry.getControllerId(), 1));
        }
    }
}
