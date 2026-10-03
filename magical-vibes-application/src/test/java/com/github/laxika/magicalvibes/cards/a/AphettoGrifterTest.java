package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AphettoGrifter.class, Forest.class})
class AphettoGrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two untapped Wizards to tap target permanent")
    void tapsTwoUntappedWizardsToTapTargetPermanent() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent wizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent nonWizard = addCreatureReady(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(grifter), null, target.getId());

        assertThat(grifter.isTapped()).isTrue();
        assertThat(wizard.isTapped()).isTrue();
        assertThat(nonWizard.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two untapped Wizards")
    void cannotActivateWithoutTwoUntappedWizards() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent nonWizard = addCreatureReady(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(grifter),
                null,
                nonWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }

    @Test
    @DisplayName("Chooses exactly two Wizards when more than two are available")
    void choosesExactlyTwoWizardsWhenMoreAreAvailable() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent firstWizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent secondWizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(grifter), null,
                target.getId());
        harness.handlePermanentChosen(player1, firstWizard.getId());
        harness.handlePermanentChosen(player1, secondWizard.getId());

        assertThat(grifter.isTapped()).isFalse();
        assertThat(firstWizard.isTapped()).isTrue();
        assertThat(secondWizard.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped or opponent-controlled Wizards cannot pay the cost")
    void tappedOrOpponentControlledWizardsCannotPayCost() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent tappedWizard = addCreatureReady(player1, new AphettoGrifter());
        tappedWizard.tap();
        Permanent opponentWizard = addCreatureReady(player2, new AphettoGrifter());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(grifter),
                null,
                opponentWizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(grifter.isTapped()).isFalse();
        assertThat(tappedWizard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player instead of a permanent")
    void cannotTargetPlayer() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent wizard = addCreatureReady(player1, new AphettoGrifter());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(grifter),
                null,
                player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grifter.isTapped()).isFalse();
        assertThat(wizard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick Wizards can pay the cost and the Grifter can target itself")
    void summoningSickWizardsCanPayCostAndTargetSelf() {
        Permanent grifter = harness.addToBattlefieldAndReturn(player1, new AphettoGrifter());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new AphettoGrifter());
        grifter.setSummoningSick(true);
        wizard.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, grifter.getId());

        assertThat(grifter.isTapped()).isTrue();
        assertThat(wizard.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(grifter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Grifter can activate using two other Wizards")
    void tappedGrifterCanActivateUsingOtherWizards() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        grifter.tap();
        Permanent firstWizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent secondWizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(firstWizard.isTapped()).isTrue();
        assertThat(secondWizard.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped permanent is a legal target")
    void canTargetAlreadyTappedPermanent() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent wizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(grifter.isTapped()).isTrue();
        assertThat(wizard.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Costs stay paid when the target leaves before resolution")
    void costsStayPaidWhenTargetLeavesBeforeResolution() {
        Permanent grifter = addCreatureReady(player1, new AphettoGrifter());
        Permanent wizard = addCreatureReady(player1, new AphettoGrifter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(grifter.isTapped()).isTrue();
        assertThat(wizard.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
