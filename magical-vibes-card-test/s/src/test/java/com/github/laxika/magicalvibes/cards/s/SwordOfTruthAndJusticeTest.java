package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfTruthAndJustice.class, GrizzlyBears.class, InvasionOfInnistrad.class, DelugeOfTheDead.class})
class SwordOfTruthAndJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from white and blue")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage puts a counter on a chosen creature, then proliferates")
    void combatDamagePutsCounterThenProliferates() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent proliferateTarget = addCreatureReady(player1, new GrizzlyBears());
        proliferateTarget.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(proliferateTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {2} attaches the Sword to a creature you control")
    void equipsToCreatureYouControl() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Combat damage puts a counter on a chosen creature and proliferates")
    void combatDamagePutsCounterAndProliferates() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(creature.getId());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when the equipped creature is blocked")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void combatDamageToBattleDoesNotTrigger() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        attacker.setAttackTarget(battle.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    void counterPlacementCannotBeDeclinedWhenCreaturesAreAvailable() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void proliferateCanChooseOpponentPermanentAndPlayerAndAddsEachCounterKind() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentCreature.setCounterCount(CounterType.CHARGE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId(), player2.getId()));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void oppositeCountersRemainUntilProliferationFinishes() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTruthAndJustice());
        sword.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
