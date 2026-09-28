package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Queues Willie Lumpkin's optional draw for the player dealt combat damage. */
@Component
public class DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var authored = (DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect) effect;
        UUID damagedPlayerId = authored.damagedPlayerId() != null
                ? authored.damagedPlayerId() : entry.getTargetId();
        UUID protectedPlayerId = authored.protectedPlayerId() != null
                ? authored.protectedPlayerId() : entry.getControllerId();
        if (damagedPlayerId == null || protectedPlayerId == null
                || !gameData.playerIds.contains(damagedPlayerId)
                || !gameData.playerIds.contains(protectedPlayerId)) {
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                damagedPlayerId,
                List.of(new DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect(
                        damagedPlayerId, protectedPlayerId)),
                "Draw a card? If you do, creatures you control can't attack "
                        + gameData.playerIdToName.get(protectedPlayerId)
                        + " or permanents they control during your next turn.",
                null, null, entry.getSourcePermanentId()));
    }
}
