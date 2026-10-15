package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuminousRebuke.class, GrizzlyBears.class})
class LuminousRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1}{W} and destroys a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, tappedCreature.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target an untapped creature when the full cost is paid")
    void fullCostWorksForUntappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, untappedCreature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Cannot pay only the reduced cost when targeting an untapped creature")
    void reducedCostDoesNotApplyToUntappedCreature() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cost reduction");
    }

    @Test
    @DisplayName("An untapped creature is destroyed after paying the full cost")
    void destroysUntappedCreatureAtFullCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapping the target after casting does not stop destruction")
    void destroysTargetThatUntapsAfterCasting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        creature.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapping the target after casting does not refund mana")
    void noRefundWhenTargetBecomesTapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, creature.getId());
        creature.tap();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduced cost also applies to a tapped creature you control")
    void canDestroyOwnTappedCreatureForReducedCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduction cannot replace the required white mana")
    void reducedCostStillRequiresWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
