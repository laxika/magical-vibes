package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillUntilCreatureThenReanimateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MillUntilCreatureThenReanimateEffect} (Helm of Obedience). Mills the target player
 * one card at a time until a creature card actually lands in their graveyard or X cards have been
 * milled. A creature card that never reaches the graveyard (a replacement moved it elsewhere) does
 * not stop the loop.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MillUntilCreatureThenReanimateEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillUntilCreatureThenReanimateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        int x = entry.getXValue();
        if (targetPlayerId == null || x <= 0) {
            return;
        }

        Card creatureCard = millUntilCreature(gameData, targetPlayerId, x);
        if (creatureCard == null) {
            return;
        }

        sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());

        permanentRemovalService.removeCardFromGraveyardById(gameData, creatureCard.getId());
        graveyardReturnSupport.putCardOntoBattlefield(gameData, entry.getControllerId(), creatureCard);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " puts ",
                creatureCard, " onto the battlefield under their control."));
    }

    /**
     * Mills up to {@code max} cards one at a time, stopping as soon as a creature card is in the
     * target player's graveyard. Returns that creature card, or null if none was put there.
     */
    private Card millUntilCreature(GameData gameData, UUID targetPlayerId, int max) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        for (int i = 0; i < max; i++) {
            if (deck == null || deck.isEmpty()) {
                return null;
            }
            Card milled = deck.getFirst();
            graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);
            if (milled.hasType(CardType.CREATURE) && graveyard != null && graveyard.contains(milled)) {
                return milled;
            }
        }
        return null;
    }
}
