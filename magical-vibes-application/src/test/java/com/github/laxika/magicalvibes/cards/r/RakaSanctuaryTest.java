package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoalitionHonorGuard;
import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakaSanctuary.class, CoalitionHonorGuard.class, Cromat.class, GaeasSkyfolk.class})
class RakaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target creature with only a white permanent")
    void dealsOneDamageWithWhitePermanentOnly() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new CoalitionHonorGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature with only a blue permanent")
    void dealsOneDamageWithBluePermanentOnly() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature with both a white and blue permanent")
    void dealsThreeDamageWithWhiteAndBluePermanents() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new CoalitionHonorGuard());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Does not trigger without a white or blue permanent")
    void doesNotTriggerWithoutWhiteOrBluePermanent() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Deals 3 damage when one permanent is both white and blue")
    void dealsThreeDamageWithOneWhiteAndBluePermanent() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new Cromat());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for a qualifying permanent controlled by an opponent")
    void doesNotTriggerForOpponentsColoredPermanent() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player2, new CoalitionHonorGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Triggers only during the controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new CoalitionHonorGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does nothing if the qualifying permanent leaves before resolution")
    void doesNothingIfQualifyingPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        Permanent whitePermanent = harness.addToBattlefieldAndReturn(player1, new CoalitionHonorGuard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(whitePermanent);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }
}
