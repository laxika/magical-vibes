package com.github.laxika.magicalvibes.cards.w;

import java.util.List;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodSymbiote.class, WirewoodElf.class, Forest.class, GrizzlyBears.class})
class WirewoodSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an Elf and untaps the target creature")
    void returnsElfAndUntapsTarget() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new WirewoodElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wirewood Elf");
        harness.assertNotOnBattlefield(player1, "Wirewood Elf");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With multiple Elves, chooses which one to return")
    void choosesWhichElfToReturn() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent elf1 = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());
        Permanent elf2 = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, elf2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wirewood Elf").getId()).isEqualTo(elf1.getId());
        harness.assertInHand(player1, "Wirewood Elf");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an Elf you control")
    void cannotActivateWithoutElf() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new WirewoodElf());
        harness.addToBattlefield(player1, new WirewoodElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, findPermanent(player1, "Wirewood Elf").getId());
        harness.passBothPriorities();

        bears.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated again on a later turn")
    void canActivateAgainOnLaterTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent elf1 = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());
        harness.addToBattlefield(player1, new WirewoodElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, elf1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        bears.tap();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        harness.assertInHand(player1, "Wirewood Elf");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new WirewoodElf());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the cost with an Elf an opponent controls")
    void cannotUseOpponentsElfForCost() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player2, new WirewoodElf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Wirewood Elf");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new WirewoodElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Wirewood Elf");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
