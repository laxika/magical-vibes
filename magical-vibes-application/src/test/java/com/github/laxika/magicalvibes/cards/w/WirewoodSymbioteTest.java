package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodSymbiote.class, FyndhornElves.class, GrizzlyBears.class, Forest.class})
class WirewoodSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an Elf and untaps the target creature")
    void returnsElfAndUntapsTarget() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new FyndhornElves());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fyndhorn Elves");
        harness.assertNotOnBattlefield(player1, "Fyndhorn Elves");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new FyndhornElves());
        harness.addToBattlefield(player1, new FyndhornElves());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, elf.getId());
        harness.passBothPriorities();

        bears.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without an Elf to return")
    void cannotActivateWithoutElf() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player1, new FyndhornElves());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fyndhorn Elves");
    }

    @Test
    @DisplayName("Cannot pay with an Elf an opponent controls")
    void cannotUseOpponentsElfForCost() {
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player2, new FyndhornElves());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Fyndhorn Elves");
        assertThat(bears.isTapped()).isTrue();
    }
}
