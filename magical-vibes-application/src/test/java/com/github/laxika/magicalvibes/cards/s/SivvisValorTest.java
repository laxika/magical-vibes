package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Batterskull;
import com.github.laxika.magicalvibes.cards.f.FallenFerromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SivvisValor.class, GrizzlyBears.class, Plains.class, ProdigalPyromancer.class,
        FallenFerromancer.class, Batterskull.class})
class SivvisValorTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects all damage to the target creature to the spell's controller")
    void redirectsAllDamageToController() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer1 = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent pyromancer2 = addCreatureReady(player1, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer1), null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, pyromancer2), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirects combat damage dealt to the target creature to the spell's controller")
    void redirectsCombatDamageToController() {
        harness.setLife(player1, 20);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be cast for its alternate cost by tapping a creature while controlling a Plains")
    void castsForAlternateCost() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent paymentCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(paymentCreature.getId()));
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(plains.isTapped()).isFalse();
        assertThat(paymentCreature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Requires a creature target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Redirected noncombat damage from an infect source gives poison counters")
    void redirectedNoncombatDamageRetainsInfect() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent source = addCreatureReady(player2, new FallenFerromancer());
        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, indexOf(player2, source), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick target can pay the tap cost while the Plains is tapped")
    void tapsSummoningSickTargetForAlternateCost() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        plains.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new SivvisValor()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(target.getId()));
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, source), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's Plains does not enable the alternate cost")
    void rejectsAlternateCostWithoutOwnPlains() {
        harness.addToBattlefield(player2, new Plains());
        Permanent payment = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SivvisValor()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(payment.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(payment.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the alternate cost")
    void rejectsTappedPaymentCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent payment = addCreatureReady(player1, new GrizzlyBears());
        payment.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SivvisValor()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(payment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the alternate cost")
    void rejectsOpponentsPaymentCreature() {
        harness.addToBattlefield(player1, new Plains());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SivvisValor()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A noncreature permanent cannot pay the alternate cost")
    void rejectsNoncreaturePaymentPermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SivvisValor()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plains.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Damage to creatures other than the target is not redirected")
    void doesNotRedirectDamageToOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent source = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, source), null, other.getId());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Redirected noncombat damage still causes the original source's lifelink life gain")
    void redirectedNoncombatDamageRetainsLifelink() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent source = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Batterskull());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, indexOf(player1, equipment), 1, null, source.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SivvisValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, source), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
