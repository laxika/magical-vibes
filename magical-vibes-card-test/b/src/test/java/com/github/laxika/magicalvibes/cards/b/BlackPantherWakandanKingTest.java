package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrimalVigor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackPantherWakandanKing.class, Forest.class, GrizzlyBears.class,
        DryadArbor.class, PrimalVigor.class})
class BlackPantherWakandanKingTest extends BaseCardTest {

    @Test
    void putsCounterOnTargetLandWhenAnotherCreatureEnters() {
        harness.addToBattlefieldAndReturn(player1, new BlackPantherWakandanKing());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void movesAllCountersAndGainsLifeAndDraws() {
        harness.addToBattlefieldAndReturn(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    void doesNotRewardActivationWhenNoCountersMove() {
        harness.addToBattlefieldAndReturn(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void requiresLandThenCreatureTargets() {
        harness.addToBattlefieldAndReturn(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsCounterOnLandWhenBlackPantherItselfEnters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        harness.setHand(player1, List.of(new BlackPantherWakandanKing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentsCreatureEntering() {
        harness.addToBattlefield(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());

        harness.enterBattlefieldAndReturn(player2, new DryadArbor());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotMoveCountersOntoTheSameCreatureLand() {
        harness.addToBattlefield(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void counterDoublingDoesNotDoubleLifeGain() {
        harness.addToBattlefield(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        harness.addToBattlefield(player1, new PrimalVigor());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void canMoveCountersOntoOpponentsCreatureAndLeavesOtherCountersBehind() {
        harness.addToBattlefield(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        land.setCounterCount(CounterType.CHARGE, 3);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotMoveCountersWhenDestinationLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void doesNotMoveCountersWhenSourceLandChangesControllerBeforeResolution() {
        harness.addToBattlefield(player1, new BlackPantherWakandanKing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(land.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
