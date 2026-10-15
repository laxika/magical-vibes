package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GripOfTheRoil.class, GrizzlyBears.class, Island.class, Shock.class})
class GripOfTheRoilTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature, skips its next untap, and draws a card")
    void tapsSkipsUntapAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GripOfTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grip of the Roil");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Casts for the Surge cost after another spell was cast this turn")
    void castsForSurgeCost() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new Shock(), new GripOfTheRoil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, firstTarget.getId());
        harness.castInstantWithAlternateCost(player1, 0, secondTarget.getId(), List.of());
        harness.passBothPriorities();

        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(secondTarget.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast for Surge before another spell was cast")
    void surgeRequiresAnotherSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GripOfTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new GripOfTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped creature stays tapped only through its controller's next untap")
    void alreadyTappedCreatureSkipsOnlyItsControllersNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GripOfTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Island");
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GripOfTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not draw when its sole target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GripOfTheRoil(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grip of the Roil");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
