package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Offers the defending player a draw when the attack trigger resolves. */
@Component
@RequiredArgsConstructor
public class DefendingPlayerMayDrawCardEffectHandler implements NormalEffectHandlerBean {
    private final GameQueryService gameQueryService;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DefendingPlayerMayDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.resolvedMayAccepted != null) {
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (accepted) drawService.resolveDrawCards(gameData, entry.getTargetId(), 1);
            return;
        }
        UUID attackedId = entry.getAttackedTargetId();
        if (attackedId == null) return;
        UUID defendingPlayerId = attackedId;
        if (!gameData.playerIds.contains(attackedId)) {
            Permanent attacked = gameQueryService.findPermanentById(gameData, attackedId);
            if (attacked == null) return;
            defendingPlayerId = gameQueryService.isBattle(gameData, attacked)
                    ? attacked.getProtectorPlayerId()
                    : gameQueryService.findPermanentController(gameData, attackedId);
        }
        if (defendingPlayerId != null) {
            entry.setTargetId(defendingPlayerId);
            entry.setNonTargeting(true);
            gameData.resolvingMayEffectFromStack = true;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), defendingPlayerId,
                    List.of(new DrawCardEffect()), "Draw a card?"));
        }
    }
}
