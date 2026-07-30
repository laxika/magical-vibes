package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AutumnsVeil;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DruidicSatchelTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing a creature card creates a 1/1 green Saproling and leaves the card on top")
    void creatureCreatesSaproling() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Revealing a land card puts it onto the battlefield under the controller's control")
    void landEntersBattlefield() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Revealing a noncreature, nonland card gains 2 life and leaves the card on top")
    void noncreatureNonlandGainsLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new AutumnsVeil(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("An empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("The ability requires tapping, so it cannot be activated twice in a turn")
    void tapCostLimitsToOnceAtATime() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
