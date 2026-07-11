package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandSharingNameWithSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Twinning Glass: "{1}, {T}: You may cast a spell from your hand without paying its mana cost if it
 * has the same name as a spell that was cast this turn."
 *
 * <p>Reuses the Counterlash may-cast-from-hand routing: one {@link PendingMayAbility} is queued per
 * eligible hand card, and accepting one casts it for free while clearing the remaining offers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastFromHandSharingNameWithSpellCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastFromHandSharingNameWithSpellCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) return;

        // Names of every spell any player cast this turn — CR: the caster doesn't matter.
        Set<String> castNames = new HashSet<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            for (Card cast : gameData.getSpellsCastThisTurn(pid)) {
                castNames.add(cast.getName());
            }
        }
        if (castNames.isEmpty()) return;

        List<Card> eligible = hand.stream()
                .filter(c -> !c.hasType(CardType.LAND) && castNames.contains(c.getName()))
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
