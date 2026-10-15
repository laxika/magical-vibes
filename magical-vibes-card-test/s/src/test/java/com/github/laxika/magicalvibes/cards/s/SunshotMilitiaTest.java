package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunshotMilitia.class, MoxOpal.class, GrizzlyBears.class, Forest.class})
class SunshotMilitiaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the source and an artifact deals 1 damage to each opponent")
    void tapsArtifactAndDamagesEachOpponent() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent artifact = addCreatureReady(player1, new MoxOpal());

        activate(militia);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(militia.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping the source and a creature satisfies the artifact-or-creature cost")
    void tapsCreatureAndDamagesOpponent() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        activate(militia);

        harness.assertLife(player2, 19);
        assertThat(militia.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two qualifying untapped permanents")
    void cannotActivateWithoutTwoQualifyingPermanents() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent land = addCreatureReady(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        assertThat(militia.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent artifact = addCreatureReady(player1, new MoxOpal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(militia.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void newlyEnteredCreaturesIncludingSourceCanPayTapCost() {
        Permanent militia = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());

        activate(militia);

        assertThat(militia.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void tappedSourceCanActivateUsingTwoOtherCreatures() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        militia.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());

        activate(militia);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    void alreadyTappedCreatureCannotPayCost() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent other = addCreatureReady(player1, new SunshotMilitia());
        other.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(militia.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsCreatureCannotPayCost() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent other = addCreatureReady(player2, new SunshotMilitia());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(militia.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent other = addCreatureReady(player1, new SunshotMilitia());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(militia.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    void damageUsesStackAndAnotherActivationRequiresEmptyStack() {
        addCreatureReady(player1, new SunshotMilitia());
        addCreatureReady(player1, new SunshotMilitia());

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void canChooseTwoOtherCreaturesAndLeaveSourceUntapped() {
        Permanent militia = addCreatureReady(player1, new SunshotMilitia());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(militia.isTapped()).isFalse();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    private void activate(Permanent militia) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(militia), null, null);
        harness.passBothPriorities();
    }
}
