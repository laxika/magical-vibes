package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandSharingNameWithSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import java.util.ArrayList;
import java.util.List;
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

        // Every spell any player cast this turn: the caster doesn't matter (Twinning Glass ruling).
        List<Card> castSpells = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            castSpells.addAll(gameData.getSpellsCastThisTurn(pid));
        }
        if (castSpells.isEmpty()) return;

        List<Card> eligible = hand.stream()
                .filter(c -> !c.hasType(CardType.LAND) && castSpells.stream().anyMatch(spell -> sameName(c, spell)))
                .toList();

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card c = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    c, controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(false)),
                    "Cast " + c.getName() + " without paying its mana cost?"
            ));
        }
    }

    /**
     * Whether {@code card} and {@code spell} have the same name: at least one name in common, and a spell with no name
     * (one cast face down) has none (CR 201.2a). A split card has each half's name (CR 709.4a); a card that has all
     * card names shares each one.
     */
    private static boolean sameName(Card card, Card spell) {
        List<String> spellNames = spell.getNames().stream().filter(name -> !name.isBlank()).toList();
        if (spell.hasAllCardNames()) {
            return card.hasAllCardNames() || card.getNames().stream().anyMatch(name -> !name.isBlank());
        }
        return spellNames.stream().anyMatch(card::hasName);
    }
}
