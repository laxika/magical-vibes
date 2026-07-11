package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LatchkeyFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Cast for its prowl cost after Faerie combat damage draws a card")
    void prowlDrawsAfterFaerieDamage() {
        setupProwl(CardSubtype.FAERIE);

        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3); // prowl {2}{U}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB draw

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Latchkey Faerie"));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cast for its prowl cost after Rogue combat damage draws a card")
    void prowlDrawsAfterRogueDamage() {
        setupProwl(CardSubtype.ROGUE);

        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3); // prowl {2}{U}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cast for its normal cost does not draw a card")
    void normalCastDoesNotDraw() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 4); // normal {3}{U}
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // No prowl cost paid — the intervening-if ETB trigger never goes on the stack.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Latchkey Faerie"));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Prowl cost is unavailable without combat damage from a Faerie or Rogue")
    void prowlUnavailableWithoutQualifyingDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3); // enough for prowl {2}{U}, not for {3}{U}

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupProwl(CardSubtype subtype) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(subtype);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
    }
}
