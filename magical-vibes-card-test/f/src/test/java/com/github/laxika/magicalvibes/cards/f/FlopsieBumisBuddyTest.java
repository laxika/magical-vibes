package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarEnthusiasts;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlopsieBumisBuddy.class, AvatarEnthusiasts.class, Forest.class})
class FlopsieBumisBuddyTest extends BaseCardTest {

    @Test
    @DisplayName("When Flopsie enters, it puts a +1/+1 counter on each creature you control")
    void putsCountersOnEachCreatureYouControl() {
        Permanent existing = addCreatureReady(player1, new AvatarEnthusiasts());
        Permanent opponent = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new FlopsieBumisBuddy(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent flopsie = findPermanent(player1, "Flopsie, Bumi's Buddy");

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flopsie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creatures you control with power 4 or greater can't be blocked by more than one creature")
    void highPowerCreatureCannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new AvatarEnthusiasts());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent blockerTwo = addCreatureReady(player2, new AvatarEnthusiasts());

        prepareDeclareBlockers();

        List<Permanent> attackers = gd.playerBattlefields.get(player1.getId());
        List<Permanent> blockers = gd.playerBattlefields.get(player2.getId());
        int attackerIndex = attackers.indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockers.indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(blockers.indexOf(blockerTwo), attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Creatures you control with power less than 4 can still be blocked by two creatures")
    void lowerPowerCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new AvatarEnthusiasts());
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent blockerTwo = addCreatureReady(player2, new AvatarEnthusiasts());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flopsie itself can be blocked by one creature")
    void flopsieCanBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent blocker = addCreatureReady(player2, new AvatarEnthusiasts());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flopsie itself cannot be blocked by two creatures")
    void flopsieCannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent blockerOne = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent blockerTwo = addCreatureReady(player2, new AvatarEnthusiasts());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Flopsie does not restrict blocking of its controller's opponents' creatures")
    void opposingHighPowerCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player2, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new AvatarEnthusiasts());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent blockerTwo = addCreatureReady(player2, new AvatarEnthusiasts());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction uses current power when blockers are declared")
    void creatureDroppingBelowFourPowerCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new AvatarEnthusiasts());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent blockerTwo = addCreatureReady(player2, new AvatarEnthusiasts());
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flopsie's restriction ends when it leaves the battlefield")
    void restrictionEndsWhenFlopsieLeaves() {
        Permanent flopsie = addCreatureReady(player1, new FlopsieBumisBuddy());
        Permanent attacker = addCreatureReady(player1, new AvatarEnthusiasts());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        Permanent blockerOne = addCreatureReady(player2, new AvatarEnthusiasts());
        Permanent blockerTwo = addCreatureReady(player2, new AvatarEnthusiasts());
        gd.playerBattlefields.get(player1.getId()).remove(flopsie);
        gd.playerGraveyards.get(player1.getId()).add(flopsie.getCard());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The entering trigger resolves after Flopsie leaves and includes creatures present at resolution")
    void enteringTriggerResolvesWithoutFlopsie() {
        Permanent existing = addCreatureReady(player1, new AvatarEnthusiasts());
        harness.castFromHand(player1, new FlopsieBumisBuddy(), "{4}{G}{G}");
        harness.passBothPriorities();
        Permanent flopsie = findPermanent(player1, "Flopsie, Bumi's Buddy");
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(flopsie);
        gd.playerGraveyards.get(player1.getId()).add(flopsie.getCard());
        Permanent newcomer = addCreatureReady(player1, new AvatarEnthusiasts());
        resolveAllTriggers();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
