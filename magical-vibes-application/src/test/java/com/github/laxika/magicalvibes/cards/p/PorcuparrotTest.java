package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LukkaCoppercoatOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Porcuparrot.class, LukkaCoppercoatOutcast.class})
class PorcuparrotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals no damage before Porcuparrot mutates")
    void dealsNoDamageBeforeMutating() {
        addCreatureReady(player1, new Porcuparrot());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage equal to the number of times Porcuparrot mutated")
    void damageScalesWithMutations() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Normally cast Porcuparrot cannot immediately pay its tap cost")
    void normalCastingDoesNotEnableImmediateActivation() {
        harness.castFromHand(player1, new Porcuparrot(), "{3}{R}");
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Activation taps Porcuparrot even when it deals zero damage")
    void zeroDamageStillPaysTapCost() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(porcuparrot.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A single mutation allows Porcuparrot to damage a creature")
    void dealsOneDamageToCreatureAfterOneMutation() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());
        Permanent target = addCreatureReady(player2, new Porcuparrot());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Porcuparrot can target itself")
    void canDamageItself() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));

        harness.activateAbility(player1, 0, null, porcuparrot.getId());
        harness.passBothPriorities();

        assertThat(porcuparrot.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Porcuparrot can damage a planeswalker")
    void canDamagePlaneswalker() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());
        Permanent lukka = harness.addToBattlefieldAndReturn(player2, new LukkaCoppercoatOutcast());
        lukka.setCounterCount(CounterType.LOYALTY, 5);
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));

        harness.activateAbility(player1, 0, null, lukka.getId());
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Mutations of another Porcuparrot do not increase this one's damage")
    void mutationsOfOtherCreaturesDoNotIncreaseDamage() {
        addCreatureReady(player1, new Porcuparrot());
        Permanent other = addCreatureReady(player1, new Porcuparrot());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, other, List.of(other.getCard()), player1.getId()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability retains the mutation count when its source leaves the battlefield")
    void usesLastKnownMutationCountAfterSourceLeaves() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(porcuparrot);
        addCreatureReady(player1, new Porcuparrot());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
