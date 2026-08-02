package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveDelayCounterFromExiledSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link RemoveDelayCounterFromExiledSpellEffect} (Ertai's Meddling's upkeep trigger).
 *
 * <p>Removes one delay counter; when the last one comes off, the card leaves exile and goes back
 * onto the stack with the X value and targets the original spell had. The card is put onto the
 * stack rather than cast, so cast triggers do not fire.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveDelayCounterFromExiledSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveDelayCounterFromExiledSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((RemoveDelayCounterFromExiledSpellEffect) effect).cardId();

        int index = indexOf(gameData, cardId);
        if (index < 0) return;

        GameData.DelayedSpellExile pending = gameData.delayedSpellExiles.get(index);

        // "if that card is exiled" — an intervening-if clause: the trigger does nothing if the card
        // has left exile, and the tracking record goes with it.
        if (gameData.findExiledCard(cardId) == null) {
            gameData.delayedSpellExiles.remove(index);
            return;
        }

        int remaining = pending.counters() - 1;
        if (remaining > 0) {
            gameData.delayedSpellExiles.set(index, new GameData.DelayedSpellExile(
                    cardId, pending.controllerId(), remaining, pending.originalEntry()));
            gameLogService.append(gameData, GameLog.cardThen(pending.originalEntry().getCard(),
                    " loses a delay counter (" + remaining + " left)."));
            return;
        }

        gameData.delayedSpellExiles.remove(index);
        gameData.removeFromExile(cardId);
        gameData.stack.add(new StackEntry(pending.originalEntry()));

        gameLogService.append(gameData, GameLog.cardThen(pending.originalEntry().getCard(),
                " loses its last delay counter and is put back onto the stack."));
        log.info("Game {} - {} returned to the stack from delay-counter exile",
                gameData.id, pending.originalEntry().getCard().getName());
    }

    private int indexOf(GameData gameData, UUID cardId) {
        for (int i = 0; i < gameData.delayedSpellExiles.size(); i++) {
            if (gameData.delayedSpellExiles.get(i).cardId().equals(cardId)) {
                return i;
            }
        }
        return -1;
    }
}
