package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlowflyInfestation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Infestation.class, BlowflyInfestation.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class InfestationTest extends BaseCardTest {

    @Test
    void entersWithBlowflyInfestationAndPutsCountersOnEveryCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent infestation = findPermanent(player1, "Infestation");
        Permanent blowfly = findPermanent(player1, "Blowfly Infestation");
        assertThat(blowfly.getCard().isToken()).isFalse();
        assertThat(infestation.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void evokePaysThreeLifeConjuresBlowflyAndSacrificesInfestation() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        PendingInteraction.ColorChoice order =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> !option.contains("sacrifice")).findFirst().orElseThrow());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Infestation");
        harness.assertInGraveyard(player1, "Infestation");
        assertThat(findPermanent(player1, "Blowfly Infestation").getCard().isToken()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void evokeCanResolveEntryAbilityBeforeSacrificeAndTriggerConjuredBlowfly() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.assertLife(player1, 17);
        harness.passBothPriorities();
        PendingInteraction.ColorChoice order =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.contains("sacrifice")).findFirst().orElseThrow());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Infestation");
        harness.assertOnBattlefield(player1, "Blowfly Infestation");
        assertThat(findPermanent(player1, "Infestation").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Infestation");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Infestation");
    }

    @Test
    void conjuredBlowflySeesCreatureKilledByEntryCounter() {
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Blowfly Infestation");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent infestation = findPermanent(player1, "Infestation");
        harness.handlePermanentChosen(player1, infestation.getId());
        resolveAllTriggers();

        assertThat(infestation.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotEvokeWithoutEnoughLife() {
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 2);
        harness.assertInHand(player1, "Infestation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void witherDealsCombatDamageAsCountersToCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent attacker = addCreatureReady(player1, new Infestation());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(6);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Infestation");
    }

    @Test
    void witherDealsNormalCombatDamageToPlayer() {
        Permanent attacker = addCreatureReady(player1, new Infestation());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 14);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
