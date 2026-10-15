package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SledgeClassSeedship.class, IntrepidTenderfoot.class, MarchOfTheMachines.class})
class SledgeClassSeedshipTest extends BaseCardTest {

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Sledge-Class Seedship")
    void stationUsesTappedCreaturePower() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());
        Permanent bears = addCreatureReady(player1, new IntrepidTenderfoot());

        harness.activateAbility(player1, battlefieldIndex(seedship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(seedship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At seven charge counters, Sledge-Class Seedship becomes a flying artifact creature")
    void sevenCountersAnimateAndGrantFlying() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());

        seedship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, seedship)).isFalse();
        assertThat(gqs.hasKeyword(gd, seedship, Keyword.FLYING)).isFalse();

        seedship.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, seedship)).isTrue();
        assertThat(gqs.hasKeyword(gd, seedship, Keyword.FLYING)).isTrue();

        seedship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, seedship)).isFalse();
        assertThat(gqs.hasKeyword(gd, seedship, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("When Sledge-Class Seedship attacks, it may put a creature from hand onto the battlefield")
    void attackPutsCreatureFromHandOntoBattlefield() {
        harness.setHand(player1, List.of(new IntrepidTenderfoot()));
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 7);

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Intrepid Tenderfoot")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationNeedsAnotherUntappedCreature() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seedship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCanTapASummoningSickCreature() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(seedship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(seedship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void stationCannotTapItselfOrAnOpponentsCreatureOrATappedCreature() {
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 7);
        harness.addToBattlefield(player2, new IntrepidTenderfoot());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seedship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(seedship.isTapped()).isFalse();
        assertThat(seedship.getCounterCount(CounterType.CHARGE)).isEqualTo(7);
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());
        addCreatureReady(player1, new IntrepidTenderfoot());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seedship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackMayDeclineToPutACreatureOntoTheBattlefield() {
        harness.setHand(player1, List.of(new IntrepidTenderfoot()));
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 7);

        declareAttack();
        harness.handleCardChosen(player1, -1);

        assertThat(countPermanents(player1, "Intrepid Tenderfoot")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void attackWithOnlyANoncreatureInHandDoesNotOfferAChoice() {
        harness.setHand(player1, List.of(new SledgeClassSeedship()));
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 7);

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Sledge-Class Seedship")).isEqualTo(1);
    }

    @Test
    void externallyAnimatedSeedshipBelowSevenCountersHasNoAttackAbility() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setHand(player1, List.of(new IntrepidTenderfoot()));
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, seedship)).isTrue();

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Intrepid Tenderfoot")).isZero();
    }

    @Test
    void attackTriggerStillResolvesAfterLosingTheChargeCounters() {
        harness.setHand(player1, List.of(new IntrepidTenderfoot()));
        Permanent seedship = addReadySeedship();
        seedship.setCounterCount(CounterType.CHARGE, 7);

        declareAttackers(List.of(battlefieldIndex(seedship)));
        seedship.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Intrepid Tenderfoot");
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadySeedship() {
        return addCreatureReady(player1, new SledgeClassSeedship());
    }

    private void declareAttack() {
        declareAttackers(List.of(battlefieldIndex(findPermanent(player1, "Sledge-Class Seedship"))));
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
