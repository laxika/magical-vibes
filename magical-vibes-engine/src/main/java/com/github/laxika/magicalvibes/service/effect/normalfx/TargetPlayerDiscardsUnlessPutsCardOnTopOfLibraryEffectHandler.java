package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * "Target player discards a card unless they put a card from their hand on top of their library."
 *
 * <p>The targeted player is asked via a "you may" ability whose accept branch puts a chosen hand
 * card on top of their library and whose decline branch discards a card. An empty hand does
 * neither. The discarded-count (0 or 1) lands on the entry's event value for a following
 * {@code MassDamageEffect(new EventValue(), true)} to read.
 */
@Component
public class TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Nothing has been discarded yet — a later EventValue reader must not see a stale value.
        entry.setEventValue(0);

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        String prompt = "Put a card from your hand on top of your library to avoid discarding? ("
                + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(effect), prompt
        ));
    }
}
