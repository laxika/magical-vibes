package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheMoat.class, NessianCourser.class, AvenMindcensor.class})
class MagusOfTheMoatTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures without flying cannot attack when Magus of the Moat is on the battlefield")
    void groundCreatureCannotAttack() {
        harness.addToBattlefield(player1, new MagusOfTheMoat());
        addCreatureReady(player2, new NessianCourser());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with flying can attack when Magus of the Moat is on the battlefield")
    void flyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new MagusOfTheMoat());
        addCreatureReady(player2, new AvenMindcensor());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The global restriction also affects the Magus controller's creatures")
    void controllersGroundCreatureCannotAttack() {
        harness.addToBattlefield(player1, new MagusOfTheMoat());
        addCreatureReady(player1, new NessianCourser());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A tapped Magus still prevents creatures without flying from attacking")
    void tappedMagusStillRestrictsAttacking() {
        var magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheMoat());
        magus.tap();
        addCreatureReady(player2, new NessianCourser());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Magus of the Moat cannot attack without flying itself")
    void magusCannotAttack() {
        addCreatureReady(player1, new MagusOfTheMoat());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures without flying can attack after Magus dies")
    void restrictionEndsWhenMagusDies() {
        var magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheMoat());
        addCreatureReady(player2, new NessianCourser());
        magus.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Magus of the Moat");

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }
}
