package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Laquatus's Creativity's draw-then-discard effect. */
@Component
@RequiredArgsConstructor
public class TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffectHandler
        implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(playerId);
        int count = hand == null ? 0 : hand.size();
        if (count == 0) {
            return;
        }

        drawService.resolveDrawCards(gameData, playerId, count);
        if (gameData.status == GameStatus.FINISHED) {
            return;
        }

        DiscardEffect discard = new DiscardEffect(count, DiscardRecipient.TARGET_PLAYER, false);
        if (gameData.interaction.isAwaitingInput() || !gameData.pendingReplacementCardDraws.isEmpty()) {
            insertAfter(entry, effect, discard);
            return;
        }
        gameData.discardCausedByOpponent = !entry.getControllerId().equals(playerId);
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, count);
    }

    /** The discard waits for an interactive draw replacement to finish, then resumes with this entry. */
    private void insertAfter(StackEntry entry, CardEffect effect, CardEffect followUp) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i).equals(effect)) {
                entry.insertEffectsToResolve(i + 1, List.of(followUp));
                return;
            }
        }
        throw new IllegalStateException("Could not locate draw-then-discard effect on stack entry");
    }
}
