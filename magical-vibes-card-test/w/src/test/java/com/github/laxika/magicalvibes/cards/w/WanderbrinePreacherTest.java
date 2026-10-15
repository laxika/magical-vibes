package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DawnsLightArcher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderbrinePreacher.class, DawnsLightArcher.class})
class WanderbrinePreacherTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Wanderbrine Preacher gains 2 life")
    void tappingWanderbrinePreacherGainsLife() {
        Permanent preacher = harness.addToBattlefieldAndReturn(player1, new WanderbrinePreacher());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(preacher);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Wanderbrine Preacher")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WanderbrinePreacher());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DawnsLightArcher());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with Wanderbrine Preacher gains life before combat damage")
    void attackingGainsLife() {
        Permanent preacher = addCreatureReady(player1, new WanderbrinePreacher());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));

        assertThat(preacher.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Untapping and tapping again creates another life-gain trigger")
    void eachTapCreatesSeparateTrigger() {
        Permanent preacher = harness.addToBattlefieldAndReturn(player1, new WanderbrinePreacher());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(preacher);
        preacher.untap();
        tap(preacher);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Each Preacher triggers only for itself")
    void anotherPreacherDoesNotDuplicateTrigger() {
        Permanent preacher = harness.addToBattlefieldAndReturn(player1, new WanderbrinePreacher());
        harness.addToBattlefield(player1, new WanderbrinePreacher());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(preacher);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An opponent's Preacher gains life only for its controller")
    void opponentPreacherGainsLifeForOpponent() {
        harness.addToBattlefield(player1, new WanderbrinePreacher());
        Permanent preacher = harness.addToBattlefieldAndReturn(player2, new WanderbrinePreacher());
        int playerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        tap(preacher);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore + 2);
    }

    @Test
    @DisplayName("A queued trigger gains life after Preacher leaves the battlefield")
    void triggerResolvesAfterPreacherLeaves() {
        Permanent preacher = harness.addToBattlefieldAndReturn(player1, new WanderbrinePreacher());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(preacher);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, preacher));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
