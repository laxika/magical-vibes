package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurfWar.class, Forest.class, GrizzlyBears.class, RayOfCommand.class, DryadArbor.class,
        LightningGreaves.class})
class TurfWarTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger's targets are one land of each player: two lands of one player are refused")
    void entersWithAContestedLandForEachPlayer() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1OtherLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2OtherLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castTurfWar();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1Land.getId(), player1OtherLand.getId(),
                player2Land.getId(), player2OtherLand.getId());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                java.util.List.of(player1Land.getId(), player1OtherLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(player1Land.getId(), player2OtherLand.getId()));
        harness.passBothPriorities();

        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
        assertThat(player1OtherLand.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(player2Land.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(player2OtherLand.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each player's only land is targeted and gets a contested counter")
    void entersAndMarksEachPlayersOnlyLand() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castTurfWar();
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(player1Land.getId(), player2Land.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
        assertThat(player2Land.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
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
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        harness.handlePermanentChosen(player2, secondLand.getId());

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

    @Test
    @DisplayName("The enter trigger targets one land of each player as it goes on the stack")
    void enterTriggerTargetsALandOfEachPlayer() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2OtherLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castTurfWar();
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(player1Land.getId(), player2OtherLand.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactlyInAnyOrder(player1Land.getId(), player2OtherLand.getId());
        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isZero();

        harness.passBothPriorities();

        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
        assertThat(player2OtherLand.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
    }

    @Test
    @DisplayName("A shrouded land can't be targeted, so with no other land for that player the trigger is removed")
    void shroudedOnlyLandRemovesTheTrigger() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent arbor = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        Permanent greaves = harness.addToBattlefieldAndReturn(player2, new LightningGreaves());
        greaves.setAttachedTo(arbor.getId());

        castTurfWar();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(arbor.getCounterCount(CounterType.CONTESTED)).isZero();
    }

    private void castTurfWar() {
        harness.setHand(player1, java.util.List.of(new TurfWar()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }
}
