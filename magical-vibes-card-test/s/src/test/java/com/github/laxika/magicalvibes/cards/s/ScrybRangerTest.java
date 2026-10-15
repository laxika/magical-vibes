package com.github.laxika.magicalvibes.cards.s;

import java.util.List;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrybRanger.class, Forest.class, AshcoatBear.class, Snapback.class})
class ScrybRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Forest and untaps the target creature")
    void returnsForestAndUntapsTarget() {
        harness.addToBattlefield(player1, new ScrybRanger());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
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
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
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
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
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
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, findPermanent(player1, "Forest").getId());
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
        harness.addToBattlefield(player1, new ScrybRanger());
        Permanent forest1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player1, forest1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        bears.tap();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new ScrybRanger());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = addCreatureReady(player2, new AshcoatBear());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the cost with a Forest an opponent controls")
    void cannotUseOpponentsForestForCost() {
        harness.addToBattlefield(player1, new ScrybRanger());
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(bears.isTapped()).isTrue();
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

    @Test
    @DisplayName("Can untap itself while tapped and summoning sick")
    void canUntapItselfWhileSummoningSick() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new ScrybRanger());
        harness.addToBattlefield(player1, new Forest());
        ranger.tap();

        harness.activateAbility(player1, 0, null, ranger.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(ranger.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(ranger.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each Ranger has its own once-per-turn activation limit")
    void eachRangerHasIndependentActivationLimit() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ScrybRanger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ScrybRanger());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        first.tap();
        second.tap();

        harness.activateAbility(player1, 0, null, first.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("Flash permits casting during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new ScrybRanger()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scryb Ranger");
    }

    @Test
    @DisplayName("Protection from blue prevents targeting with Snapback")
    void cannotBeTargetedByBlueSpell() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new ScrybRanger());
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ranger.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Scryb Ranger");
        harness.assertInHand(player2, "Snapback");
    }

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new ScrybRanger());
        addCreatureReady(player2, new AshcoatBear());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

}
