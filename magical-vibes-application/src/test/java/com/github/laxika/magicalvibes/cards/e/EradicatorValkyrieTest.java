package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EradicatorValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Boast makes each opponent sacrifice a creature")
    void boastSacrificesOpponentsCreature() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        addCreatureReady(player2, new GrizzlyBears());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eradicator Valkyrie");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Boast makes each opponent sacrifice a planeswalker")
    void boastSacrificesOpponentsPlaneswalker() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana Vess");
    }

    @Test
    @DisplayName("Boast does not sacrifice noncreature, nonplaneswalker permanents")
    void boastIgnoresLands() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        harness.addToBattlefield(player2, new Forest());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Boast requires the Valkyrie to have attacked this turn")
    void boastRequiresAttack() {
        addCreatureReady(player1, new EradicatorValkyrie());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }
}
