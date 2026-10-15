package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroundedForLife.class, GrizzlyBears.class})
class GroundedForLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1}{W} and destroys a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new GroundedForLife()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, tappedCreature.getId());

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target an untapped creature when the full cost is paid")
    void fullCostWorksForUntappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GroundedForLife()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, untappedCreature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot pay only the reduced cost when targeting an untapped creature")
    void reducedCostDoesNotApplyToUntappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GroundedForLife()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Destroys the creature even if it untaps after the reduced cost is paid")
    void untappingAfterCastingDoesNotPreventDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new GroundedForLife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        creature.untap();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can destroy an untapped creature controlled by the caster")
    void destroysOwnUntappedCreatureAtFullCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GroundedForLife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The tapped-target discount does not remove the white mana requirement")
    void reducedCostStillRequiresWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new GroundedForLife()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
