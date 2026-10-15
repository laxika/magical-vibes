package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathGreetersChampion.class, BurnishedHart.class})
class DeathGreetersChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants double strike")
    void backsUpAnotherCreature() {
        Permanent hart = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        castChampion();

        resolveEtbTargeting(hart);

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hart.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant double strike")
    void backingUpSourceDoesNotGrantDoubleStrike() {
        castChampion();
        Permanent champion = findPermanent(player1, "Death-Greeter's Champion");

        resolveEtbTargeting(champion);

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(champion.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Backup's granted double strike expires at the end of the turn")
    void grantedDoubleStrikeExpiresAtEndOfTurn() {
        Permanent hart = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        castChampion();
        resolveEtbTargeting(hart);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hart.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dash grants haste and returns the Champion to its owner's hand at end step")
    void dashGrantsHasteAndReturnsToHand() {
        harness.setHand(player1, List.of(new DeathGreetersChampion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Death-Greeter's Champion");
        harness.handlePermanentChosen(player1, champion.getId());
        harness.passBothPriorities();

        assertThat(champion.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class))
                .anyMatch(action -> action.affectedPermanentId().equals(champion.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Death-Greeter's Champion");
        harness.assertNotOnBattlefield(player1, "Death-Greeter's Champion");
    }

    @Test
    @DisplayName("Backup can grant its counter and double strike to an opponent's creature")
    void backsUpOpponentsCreature() {
        Permanent hart = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        castChampion();

        resolveEtbTargeting(hart);

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hart.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(hart.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Dash schedules its return when the spell resolves, before backup resolves")
    void dashReturnDoesNotDependOnBackupResolution() {
        harness.setHand(player1, List.of(new DeathGreetersChampion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Death-Greeter's Champion");
        harness.handlePermanentChosen(player1, champion.getId());

        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class))
                .anyMatch(action -> action.affectedPermanentId().equals(champion.getId()));
    }

    @Test
    @DisplayName("Casting normally does not grant haste or return the Champion at end step")
    void normalCastStaysOnBattlefield() {
        castChampion();
        Permanent champion = findPermanent(player1, "Death-Greeter's Champion");
        resolveEtbTargeting(champion);

        assertThat(champion.hasKeyword(Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Death-Greeter's Champion");
        harness.assertNotInHand(player1, "Death-Greeter's Champion");
    }

    private void castChampion() {
        harness.castFromHand(player1, new DeathGreetersChampion(), "{2}{R}");
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
