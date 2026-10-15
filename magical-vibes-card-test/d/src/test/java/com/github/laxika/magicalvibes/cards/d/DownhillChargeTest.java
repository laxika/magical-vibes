package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SkySpirit;
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

@CardUsed({DownhillCharge.class, Island.class, Mountain.class, SkySpirit.class})
class DownhillChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+0 for each Mountain controlled")
    void boostsByNumberOfMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts only Mountains controlled by the spell's controller")
    void countsOnlyMountainsControlledByCaster() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts Mountains when the spell resolves")
    void countsMountainsAtResolution() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Alternate cost sacrifices a Mountain before counting remaining Mountains")
    void castsBySacrificingAMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(mountain.getId()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Mountain")
    void alternateCostRejectsNonMountain() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a creature target")
    void rejectsNonCreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Resolves with no boost when the caster controls no Mountains")
    void resolvesWithNoMountains() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Downhill Charge");
    }

    @Test
    @DisplayName("Can sacrifice the last Mountain even when it is tapped")
    void sacrificesLastTappedMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Downhill Charge");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Mountain for the alternate cost")
    void alternateCostRejectsOpponentsMountain() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent opponentsMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(opponentsMountain.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("The resolved boost does not change when another Mountain enters")
    void resolvedBoostRemainsFixed() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkySpirit());
        harness.setHand(player1, List.of(new DownhillCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
    }
}
