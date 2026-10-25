package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "That player loses N life unless they discard a card" (Painful Quandary). A player with an empty hand loses the
 * life at once, through {@link LifeSupport} like every other life loss; one with cards is asked through the may
 * ability system.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life loss of a player with an empty hand is its first change
 * (asking the player changes nothing yet), so a Lich's Duel Mastery that loss would sacrifice is asked about
 * (CR 616.1) before any life is lost.
 */
@Component
@RequiredArgsConstructor
public class LoseLifeUnlessDiscardEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeUnlessDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeUnlessDiscardEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId, e.lifeLoss(), entry.getCard().getName());
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to discard."));
            return;
        }

        String prompt = "Discard a card? If you don't, you lose " + e.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt, null, entry.getControllerId()
        ));
    }
}
