package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTwoRepeatIfSharedColorEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MillTwoRepeatIfSharedColorEffect} (Grindstone). Mills two cards at a time and
 * repeats while exactly the two cards put into the graveyard this way share a color. Cards that a
 * replacement effect kept out of the graveyard are not "milled this way", so they cannot continue
 * the loop.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MillTwoRepeatIfSharedColorEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTwoRepeatIfSharedColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        while (deck != null && !deck.isEmpty()) {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, targetPlayerId, 2);
            if (!shareAColor(milled)) {
                return;
            }
        }
    }

    /** True only when exactly two cards reached the graveyard and they have a color in common. */
    private boolean shareAColor(List<Card> milled) {
        if (milled.size() != 2) {
            return false;
        }
        return milled.getFirst().getColors().stream().anyMatch(milled.get(1).getColors()::contains);
    }
}
