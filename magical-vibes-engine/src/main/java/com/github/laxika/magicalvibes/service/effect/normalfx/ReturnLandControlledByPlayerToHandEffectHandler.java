package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
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
 * The player carried on the trigger's {@code targetId} returns a land they control to its
 * owner's hand (Mana Breach). Reuses the shared {@code BounceCreature} choice context to
 * prompt that player and return the chosen land via {@code handleBounceCreature}.
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
        UUID playerId = entry.getTargetId();
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
            gameBroadcastService.logAndBroadcast(gameData, playerName + " controls no lands to return.");
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(playerId));
        playerInputService.beginPermanentChoice(gameData, playerId, landIds,
                "Choose a land to return to its owner's hand.");
    }
}
