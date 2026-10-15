package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ShadowedCaravel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlowThrough.class, GrizzlyBears.class, LlanowarElves.class, ShadowedCaravel.class})
class PlowThroughTest extends BaseCardTest {

    @Test
    @DisplayName("Fight mode makes the selected creatures fight")
    void fightMode() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{0}, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Llanowar Elves")));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Vehicle mode destroys the selected Vehicle")
    void vehicleMode() {
        harness.addToBattlefield(player2, new ShadowedCaravel());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 1,
                harness.getPermanentId(player2, "Shadowed Caravel"));

        harness.assertNotOnBattlefield(player2, "Shadowed Caravel");
        harness.assertInGraveyard(player2, "Shadowed Caravel");
    }

    @Test
    @DisplayName("Fight mode requires a creature you control and an opposing creature")
    void fightModeRejectsInvalidTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, new int[]{0}, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vehicle mode rejects a non-Vehicle permanent")
    void vehicleModeRejectsNonVehicle() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vehicle mode can destroy a Vehicle you control")
    void vehicleModeCanDestroyOwnVehicle() {
        harness.addToBattlefield(player1, new ShadowedCaravel());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 1,
                harness.getPermanentId(player1, "Shadowed Caravel"));

        harness.assertNotOnBattlefield(player1, "Shadowed Caravel");
        harness.assertInGraveyard(player1, "Shadowed Caravel");
    }

    @Test
    @DisplayName("Fight deals damage in both directions even when both creatures die")
    void fightCanKillBothCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{0}, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fight mode rejects an opposing creature as the first target")
    void fightRejectsOpposingFirstTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, new int[]{0}, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Llanowar Elves"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fight mode rejects your own creature as the second target")
    void fightRejectsOwnSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, new int[]{0}, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player1, "Llanowar Elves"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No fight damage is dealt when the opposing target leaves before resolution")
    void fightDoesNothingWhenOpponentLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new PlowThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{0}, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Llanowar Elves")));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Plow Through");
    }
}
