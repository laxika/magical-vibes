package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrybRanger.class, Forest.class, GrizzlyBears.class})
class ScrybRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Forest and untaps the target creature")
    void returnsForestAndUntapsTarget() {
        harness.addToBattlefield(player1, new ScrybRanger());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With multiple Forests, chooses which one to return")
    void choosesWhichForestToReturn() {
        harness.addToBattlefield(player1, new ScrybRanger());
        Permanent forest1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent forest2 = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, forest2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").getId()).isEqualTo(forest1.getId());
        harness.assertInHand(player1, "Forest");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a Forest you control")
    void cannotActivateWithoutForest() {
        harness.addToBattlefield(player1, new ScrybRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefield(player1, new ScrybRanger());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, findPermanent(player1, "Forest").getId());
        harness.passBothPriorities();

        bears.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ScrybRanger());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Forest");
    }

}
