package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Maelstrom Archangel: "Whenever this creature deals combat damage to a player, you may cast a
 * spell from your hand without paying its mana cost."
 *
 * <p>Reuses the Counterlash may-cast-from-hand routing: one {@link PendingMayAbility} is queued per
 * nonland hand card, and accepting one casts it for free while clearing the remaining offers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastAnySpellFromHandWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastAnySpellFromHandWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) return;

        List<Card> eligible = hand.stream()
                .filter(c -> !c.hasType(CardType.LAND))
                .toList();

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card c = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    c, controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                    "Cast " + c.getName() + " without paying its mana cost?"
            ));
        }
    }
}
