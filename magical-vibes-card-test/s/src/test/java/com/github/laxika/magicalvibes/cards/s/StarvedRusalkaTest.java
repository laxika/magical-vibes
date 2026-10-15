package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({StarvedRusalka.class, GhostWarden.class})
class StarvedRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and gains 1 life")
    void sacrificesCreatureAndGainsLife() {
        addCreatureReady(player1, new StarvedRusalka());
        Permanent fodder = addCreatureReady(player1, new GhostWarden());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Ghost Warden");
        harness.assertNotOnBattlefield(player1, "Ghost Warden");
        harness.assertNotInGraveyard(player1, "Starved Rusalka");
    }

    @Test
    @DisplayName("Can sacrifice itself as the creature cost")
    void canSacrificeItself() {
        addCreatureReady(player1, new StarvedRusalka());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Starved Rusalka");
    }

    @Test
    @DisplayName("Requires green mana to activate")
    void requiresGreenMana() {
        addCreatureReady(player1, new StarvedRusalka());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent rusalka = harness.addToBattlefieldAndReturn(player1, new StarvedRusalka());
        rusalka.tap();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Starved Rusalka");
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and only the controller gains life")
    void sacrificeIsPaidBeforeLifeGain() {
        addCreatureReady(player2, new StarvedRusalka());
        Permanent fodder = addCreatureReady(player2, new GhostWarden());
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, fodder.getId());

        harness.assertInGraveyard(player2, "Ghost Warden");
        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Starved Rusalka");
    }
}
