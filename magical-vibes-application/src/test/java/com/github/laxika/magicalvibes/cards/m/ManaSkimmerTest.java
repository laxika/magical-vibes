package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSkimmer.class, Forest.class, FireWhip.class})
class ManaSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to a player offers only that player's lands and locks the chosen land")
    void damagesPlayerAndLocksChosenLand() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent damagedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(damagedLand.getId());

        harness.handlePermanentChosen(player1, damagedLand.getId());
        harness.passBothPriorities();

        assertThat(damagedLand.isTapped()).isTrue();
        assertThat(damagedLand.getSkipUntapCount()).isEqualTo(1);
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability resolves without a choice when the damaged player controls no lands")
    void resolvesWithoutChoiceWhenDamagedPlayerControlsNoLand() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        skimmer.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The chosen land stays tapped through its next untap step")
    void chosenLandStaysTappedThroughNextUntapStep() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent damagedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, damagedLand.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(damagedLand.isTapped()).isTrue();
        assertThat(damagedLand.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("An already tapped land can be targeted and untaps after the one skipped step")
    void alreadyTappedLandSkipsOnlyOneUntapStep() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setTapped(true);
        skimmer.setAttacking(true);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger still taps and locks its target after Mana Skimmer leaves")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(skimmer);
        gd.playerGraveyards.get(player1.getId()).add(skimmer.getCard());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A land no longer controlled by the damaged player is an illegal target")
    void targetChangingControllerBeforeResolutionIsNotTappedOrLocked() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerBattlefields.get(player1.getId()).add(land);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two Mana Skimmer triggers on one land skip the same next untap step")
    void multipleTriggersDoNotSkipAdditionalUntapSteps() {
        Permanent first = addCreatureReady(player1, new ManaSkimmer());
        Permanent second = addCreatureReady(player1, new ManaSkimmer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        first.setAttacking(true);
        second.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage to its controller targets that controller's lands")
    void noncombatDamageToControllerTriggersLandLock() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        aura.setAttachedTo(skimmer.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        assertThat(ownLand.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(ownLand.isTapped()).isTrue();
    }
}
