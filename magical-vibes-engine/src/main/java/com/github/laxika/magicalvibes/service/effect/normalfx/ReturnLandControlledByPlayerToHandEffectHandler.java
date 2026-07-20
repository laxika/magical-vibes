package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLandControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * A player returns a land they control to its owner's hand. The acting player is the one carried
 * on the entry's {@code targetId} (Mana Breach — "that player" i.e. the caster) or, when no target
 * is set, the resolving controller (Kefnet the Mindful — "you may return a land you control",
 * wrapped in a {@code MayEffect} so the whole return is optional). Reuses the shared
 * {@code BounceCreature} choice context to prompt that player and return the chosen land.
 */
@Component
@RequiredArgsConstructor
public class ReturnLandControlledByPlayerToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnLandControlledByPlayerToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<UUID> landIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().hasType(CardType.LAND)) {
                    landIds.add(p.getId());
                }
            }
        }

        if (landIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " controls no lands to return."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(playerId));
        playerInputService.beginPermanentChoice(gameData, playerId, landIds,
                "Choose a land to return to its owner's hand.");
    }
}
