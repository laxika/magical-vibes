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
 * repeats while any two cards milled this way share a color, including cards moved to exile
 * by a replacement effect.
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
            List<Card> milled = graveyardService.resolveMillPlayerIncludingExiled(gameData, targetPlayerId, 2);
            if (!shareAColor(milled)) {
                return;
            }
        }
    }

    /** Mill modifiers can make the batch larger than two cards. */
    private boolean shareAColor(List<Card> milled) {
        for (int i = 0; i < milled.size(); i++) {
            for (int j = i + 1; j < milled.size(); j++) {
                if (milled.get(i).getColors().stream().anyMatch(milled.get(j).getColors()::contains)) {
                    return true;
                }
            }
        }
        return false;
    }
}
