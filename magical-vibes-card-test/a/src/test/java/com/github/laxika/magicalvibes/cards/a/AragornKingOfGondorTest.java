package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AragornKingOfGondor.class, WoodElves.class})
class AragornKingOfGondorTest extends BaseCardTest {

    @Test
    void entersAndMakesItsControllerTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new AragornKingOfGondor());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void attacksAndMakesAllCreaturesUnableToBlockWhileMonarch() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent targeted = addCreatureReady(player2, new WoodElves());
        Permanent other = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targeted.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, targeted, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, other, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void attacksOnlyMakesTheChosenCreatureUnableToBlockWhenNotMonarch() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent targeted = addCreatureReady(player2, new WoodElves());
        Permanent other = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player2.getId();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, targeted.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, targeted, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, other, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void canDeclineTheOptionalTarget() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent blocker = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void decliningTargetWithoutBeingMonarchLeavesCreaturesAbleToBlock() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent blocker = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player2.getId();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void becomingMonarchAfterAttackingPreventsAllBlocking() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent targeted = addCreatureReady(player2, new WoodElves());
        Permanent other = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player2.getId();

        declareAttackers(List.of(0));
        gd.monarchPlayerId = player1.getId();
        harness.handlePermanentChosen(player1, targeted.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, other, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void losingMonarchAfterAttackingOnlyPreventsTheTargetFromBlocking() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent targeted = addCreatureReady(player2, new WoodElves());
        Permanent other = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));
        gd.monarchPlayerId = player2.getId();
        harness.handlePermanentChosen(player1, targeted.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, targeted, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, other, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void illegalChosenTargetPreventsAllEffectsEvenWhileMonarch() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        Permanent targeted = addCreatureReady(player2, new WoodElves());
        Permanent other = addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, targeted.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, targeted);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, other, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void monarchRestrictionAlsoAppliesToCreaturesEnteringAfterResolution() {
        Permanent aragorn = addCreatureReady(player1, new AragornKingOfGondor());
        addCreatureReady(player2, new WoodElves());
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        Permanent lateBlocker = harness.addToBattlefieldAndReturn(player2, new WoodElves());

        assertThat(bls.canBlockAttacker(gd, lateBlocker, aragorn,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
