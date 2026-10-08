package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AdventurersAirship;
import com.github.laxika.magicalvibes.cards.d.DwarvenCastleGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoureNotAlone.class, AdventurersAirship.class, DwarvenCastleGuard.class})
class YoureNotAloneTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 when the spell's controller controls fewer than three creatures")
    void givesPlusTwoPlusTwoBelowThreshold() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        castYoureNotAlone(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Target creature gets +4/+4 when the spell's controller controls three creatures")
    void givesPlusFourPlusFourAtThreshold() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        castYoureNotAlone(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The creature count is checked when the spell resolves")
    void checksCreatureCountAtResolution() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new YoureNotAlone()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        addCreatureReady(player1, new DwarvenCastleGuard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        castYoureNotAlone(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AdventurersAirship());
        harness.setHand(player1, List.of(new YoureNotAlone()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void ownTargetCountsTowardThreeCreatures() {
        Permanent target = addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());

        castYoureNotAlone(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void opposingCreaturesAndNoncreatureVehiclesDoNotCount() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        addCreatureReady(player2, new DwarvenCastleGuard());
        addCreatureReady(player2, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        harness.addToBattlefield(player1, new AdventurersAirship());

        castYoureNotAlone(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void losingCreatureBeforeResolutionUsesSmallerBoost() {
        Permanent target = addCreatureReady(player2, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        Permanent removed = addCreatureReady(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new YoureNotAlone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(removed);
        harness.setExile(player1, List.of(removed.getCard()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void creatureCountChangesAfterResolutionDoNotChangeBoost() {
        Permanent target = addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        castYoureNotAlone(target);

        addCreatureReady(player1, new DwarvenCastleGuard());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void moreThanThreeCreaturesStillGivesOnlyPlusFour() {
        Permanent target = addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());

        castYoureNotAlone(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    void removedTargetDoesNotReceiveEitherBoost() {
        Permanent target = addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        addCreatureReady(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new YoureNotAlone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "You're Not Alone");
    }

    private void castYoureNotAlone(Permanent target) {
        harness.setHand(player1, List.of(new YoureNotAlone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
