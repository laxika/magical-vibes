package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinElectromancer;
import com.github.laxika.magicalvibes.cards.r.RagingGoblinoids;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PumpkinBombardment.class, Forest.class, GrizzlyBears.class,
        GoblinElectromancer.class, RagingGoblinoids.class})
class PumpkinBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and deals 3 damage to target creature")
    void discardsCardAndDealsThreeDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        castPumpkinBombardment(target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pays {2} instead of discarding and deals 3 damage")
    void paysManaInsteadOfDiscarding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castPumpkinBombardment(target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without a discard or enough mana for the alternate cost")
    void cannotCastWithoutDiscardOrMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> castPumpkinBombardment(target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card or pay {2}");
    }

    @Test
    @DisplayName("Rejects a player as the target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> castPumpkinBombardment(player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPumpkinBombardment(UUID targetId, Integer discardHandCardIndex) {
        harness.castInstantWithDiscard(player1, 0, targetId, discardHandCardIndex);
    }

    @Test
    @DisplayName("Marks exactly 3 damage on a surviving creature you control")
    void dealsExactlyThreeDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RagingGoblinoids());
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        castPumpkinBombardment(target.getId(), 1);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Goblinoids");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("May pay mana even when a card is available to discard")
    void paysManaAndKeepsOtherCardInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblinoids());
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castPumpkinBombardment(target.getId(), null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Can discard a nonland card to pay the additional cost")
    void discardsNonlandCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblinoids());
        harness.setHand(player1, List.of(new PumpkinBombardment(), new RagingGoblinoids()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        castPumpkinBombardment(target.getId(), 1);

        harness.assertInGraveyard(player1, "Raging Goblinoids");
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot discard the spell itself to pay its additional cost")
    void cannotDiscardItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblinoids());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> castPumpkinBombardment(target.getId(), 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Pumpkin Bombardment");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cost reduction applies to the additional mana payment")
    void costReductionAppliesToAdditionalMana() {
        harness.addToBattlefield(player1, new GoblinElectromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblinoids());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castPumpkinBombardment(target.getId(), null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("An illegal target does not refund the discarded card")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblinoids());
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        castPumpkinBombardment(target.getId(), 1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Raging Goblinoids");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Pumpkin Bombardment");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
