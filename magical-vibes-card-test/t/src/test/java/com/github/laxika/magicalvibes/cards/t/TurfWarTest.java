package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurfWar.class, Forest.class, GrizzlyBears.class, RayOfCommand.class})
class TurfWarTest extends BaseCardTest {

    @Test
    void entersWithAContestedLandForEachPlayer() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1OtherLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2OtherLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, java.util.List.of(new TurfWar()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isZero();
        harness.handlePermanentChosen(player1, player1Land.getId());
        harness.handlePermanentChosen(player1, player2OtherLand.getId());
        harness.passBothPriorities();

        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
        assertThat(player1OtherLand.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(player2Land.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(player2OtherLand.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
    }

    @Test
    void combatDamageToOpponentTransfersAndUntapsContestedLand() {
        harness.addToBattlefieldAndReturn(player1, new TurfWar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setCounterCount(CounterType.CONTESTED, 1);
        land.tap();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void combatDamageToControllerTransfersOwnContestedLandToAttacker() {
        harness.addToBattlefieldAndReturn(player1, new TurfWar());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CONTESTED, 1);
        land.tap();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerWhenDamagedOpponentHasNoContestedLand() {
        harness.addToBattlefield(player1, new TurfWar());
        harness.addToBattlefield(player2, new Forest());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenDamagedControllerHasNoContestedLand() {
        harness.addToBattlefield(player1, new TurfWar());
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureControllerChoosesAmongContestedLandsOnResolution() {
        harness.addToBattlefield(player1, new TurfWar());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstLand.setCounterCount(CounterType.CONTESTED, 1);
        secondLand.setCounterCount(CounterType.CONTESTED, 1);
        secondLand.tap();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        harness.handleMultiplePermanentsChosen(player2, java.util.List.of(secondLand.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstLand).doesNotContain(secondLand);
        assertThat(secondLand.isTapped()).isFalse();
        assertThat(secondLand.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
    }

    @Test
    void doesNothingIfLastContestedCounterIsRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new TurfWar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setCounterCount(CounterType.CONTESTED, 1);
        land.tap();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        land.setCounterCount(CounterType.CONTESTED, 0);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void usesCreatureControllerAtResolutionWhenControlChangesInResponse() {
        harness.addToBattlefield(player1, new TurfWar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setCounterCount(CounterType.CONTESTED, 1);
        land.tap();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.setHand(player2, java.util.List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.resolveCombatDamage();

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(land.isTapped()).isFalse();
    }
}
