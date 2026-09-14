package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiversionaryTactics.class, GaeasSkyfolk.class})
class DiversionaryTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two creatures you control as a cost and taps the target creature")
    void tapsTwoCreaturesAndTargetCreature() {
        Permanent tactics = harness.addToBattlefieldAndReturn(player1, new DiversionaryTactics());
        Permanent firstCostCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondCostCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tactics);
        harness.activateAbility(player1, tacticsIndex, null, target.getId());

        assertThat(firstCostCreature.isTapped()).isTrue();
        assertThat(secondCostCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(tactics.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(firstCostCreature.isTapped()).isTrue();
        assertThat(secondCostCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two untapped creatures you control")
    void cannotActivateWithoutTwoUntappedControlledCreatures() {
        Permanent tactics = harness.addToBattlefieldAndReturn(player1, new DiversionaryTactics());
        Permanent untappedCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent tappedCreature = addCreatureReady(player1, new GaeasSkyfolk());
        tappedCreature.tap();
        Permanent opponentCreature = addCreatureReady(player2, new GaeasSkyfolk());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tactics);
        assertThatThrownBy(() -> harness.activateAbility(player1, tacticsIndex, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(untappedCreature.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent tactics = harness.addToBattlefieldAndReturn(player1, new DiversionaryTactics());
        Permanent firstCostCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondCostCreature = addCreatureReady(player1, new GaeasSkyfolk());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tactics);
        assertThatThrownBy(() -> harness.activateAbility(player1, tacticsIndex, null, tactics.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(firstCostCreature.isTapped()).isFalse();
        assertThat(secondCostCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps exactly two creatures when more than two are available")
    void tapsExactlyTwoCreaturesWhenMoreAreAvailable() {
        Permanent tactics = harness.addToBattlefieldAndReturn(player1, new DiversionaryTactics());
        Permanent firstCostCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondCostCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent thirdCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent target = addCreatureReady(player2, new GaeasSkyfolk());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tactics);
        harness.activateAbility(player1, tacticsIndex, null, target.getId());
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        harness.handlePermanentChosen(player1, secondCostCreature.getId());
        harness.passBothPriorities();

        assertThat(firstCostCreature.isTapped()).isTrue();
        assertThat(secondCostCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature tapped as the cost can also be the target")
    void creatureTappedAsCostCanAlsoBeTarget() {
        Permanent tactics = harness.addToBattlefieldAndReturn(player1, new DiversionaryTactics());
        Permanent targetAndCost = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondCostCreature = addCreatureReady(player1, new GaeasSkyfolk());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tactics);
        harness.activateAbility(player1, tacticsIndex, null, targetAndCost.getId());
        harness.passBothPriorities();

        assertThat(targetAndCost.isTapped()).isTrue();
        assertThat(secondCostCreature.isTapped()).isTrue();
    }
}
