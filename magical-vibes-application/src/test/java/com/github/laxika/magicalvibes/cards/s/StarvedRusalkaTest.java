package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarvedRusalka.class, GrizzlyBears.class})
class StarvedRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and gains 1 life")
    void sacrificesCreatureAndGainsLife() {
        addCreatureReady(player1, new StarvedRusalka());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
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
}
