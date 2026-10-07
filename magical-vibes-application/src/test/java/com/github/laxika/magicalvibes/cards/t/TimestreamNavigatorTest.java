package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimestreamNavigator.class, Forest.class, GrizzlyBears.class})
class TimestreamNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without the city's blessing")
    void cannotActivateWithoutBlessing() {
        addCreatureReady(player1, new TimestreamNavigator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("city's blessing");
    }

    @Test
    @DisplayName("Ascend grants the city's blessing at ten permanents")
    void ascendGrantsBlessing() {
        addCreatureReady(player1, new TimestreamNavigator());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("Activation puts the Navigator on the library bottom and grants an extra turn")
    void activationPutsNavigatorOnBottomAndGrantsExtraTurn() {
        addCreatureReady(player1, new TimestreamNavigator());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Timestream Navigator");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName())
                .isEqualTo("Timestream Navigator");

        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void navigatorEnteringAsTenthPermanentGrantsBlessingImmediately() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.enterBattlefieldAndReturn(player1, new TimestreamNavigator());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ninePermanentsDoNotGrantBlessing() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.enterBattlefieldAndReturn(player1, new TimestreamNavigator());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The blessing allows activation below ten permanents during an opponent's turn")
    void canActivateBelowTenPermanentsDuringOpponentsTurn() {
        TimestreamNavigator navigator = new TimestreamNavigator();
        Forest libraryCard = new Forest();
        addCreatureReady(player1, navigator);
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Timestream Navigator");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, navigator);
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void summoningSicknessPreventsActivationEvenWithBlessing() {
        harness.addToBattlefield(player1, new TimestreamNavigator());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Timestream Navigator");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void insufficientBlueManaDoesNotMoveNavigator() {
        addCreatureReady(player1, new TimestreamNavigator());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Timestream Navigator");
        assertThat(findPermanent(player1, "Timestream Navigator").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }
}
