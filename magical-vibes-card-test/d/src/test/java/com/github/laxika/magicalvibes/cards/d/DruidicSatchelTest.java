package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AutumnsVeil;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidicSatchel.class, AutumnsVeil.class, Forest.class, LlanowarElves.class, DryadArbor.class})
class DruidicSatchelTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing a creature card creates a 1/1 green Saproling and leaves the card on top")
    void creatureCreatesSaproling() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Llanowar Elves");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Revealing a land card puts it onto the battlefield under the controller's control")
    void landEntersBattlefield() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Revealing a noncreature, nonland card gains 2 life and leaves the card on top")
    void noncreatureNonlandGainsLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new AutumnsVeil(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
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

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("A creature land both creates a Saproling and enters the battlefield")
    void creatureLandGetsBothBenefits() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        var arbor = new DryadArbor();
        var forest = new Forest();
        harness.setLibrary(player1, List.of(arbor, forest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
        harness.assertOnBattlefield(player1, "Dryad Arbor");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The top card is determined when the ability resolves")
    void usesTopCardAtResolution() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        var forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Saproling");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The ability uses its controller's library and life total")
    void usesSecondPlayersLibraryAndLifeTotal() {
        harness.addToBattlefield(player2, new DruidicSatchel());
        var creature = new LlanowarElves();
        var spell = new AutumnsVeil();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spell);
        harness.assertNotOnBattlefield(player1, "Saproling");
        harness.assertNotOnBattlefield(player2, "Saproling");
    }

    @Test
    @DisplayName("The ability requires tapping, so it cannot be activated twice in a turn")
    void tapCostLimitsToOnceAtATime() {
        harness.addToBattlefield(player1, new DruidicSatchel());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
