package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LatchkeyFaerie.class)
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
        harness.assertOnBattlefield(player1, "Latchkey Faerie");
        harness.assertInHand(player1, "Latchkey Faerie");
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
        harness.assertInHand(player1, "Latchkey Faerie");
    }

    @Test
    @DisplayName("Cast for its normal cost does not draw a card")
    void normalCastDoesNotDraw() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(new LatchkeyFaerie()));

        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 4); // normal {3}{U}
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // No prowl cost paid — the intervening-if ETB trigger never goes on the stack.
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Latchkey Faerie");
        harness.assertNotInHand(player1, "Latchkey Faerie");
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

    @Test
    @DisplayName("Choosing the normal cost with prowl available does not draw")
    void normalCostWithProwlAvailableDoesNotDraw() {
        setupProwl(CardSubtype.FAERIE);
        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Latchkey Faerie");
        harness.assertNotInHand(player1, "Latchkey Faerie");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage from an unrelated creature type does not enable prowl")
    void unrelatedSubtypeDoesNotEnableProwl() {
        setupProwl(CardSubtype.WARRIOR);
        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Latchkey Faerie");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's qualifying combat damage does not enable your prowl")
    void opponentsDamageDoesNotEnableProwl() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player2.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ROGUE);
        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Latchkey Faerie");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prowl still requires paying its full mana cost")
    void prowlRequiresEnoughMana() {
        setupProwl(CardSubtype.ROGUE);
        harness.setHand(player1, List.of(new LatchkeyFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Latchkey Faerie");
        assertThat(gd.stack).isEmpty();
    }

    private void setupProwl(CardSubtype subtype) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(subtype);
        harness.setLibrary(player1, List.of(new LatchkeyFaerie()));
    }
}
