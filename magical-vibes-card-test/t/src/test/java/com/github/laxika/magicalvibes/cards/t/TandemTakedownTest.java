package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRavnica;
import com.github.laxika.magicalvibes.cards.w.WrennAndRealmbreaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class,
        InvasionOfRavnica.class, TandemTakedown.class, WrennAndRealmbreaker.class})
class TandemTakedownTest extends BaseCardTest {

    @Test
    void boostsOneCreatureBeforeItDealsPowerDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(victim.getId(), source.getId()));

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(victim.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void bothSelectedCreaturesAreBoostedAndDealDamage() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(victim.getId(), firstSource.getId(), secondSource.getId()));

        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(firstSource.getEffectivePower()).isEqualTo(4);
        assertThat(secondSource.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void canDealDamageToABattle() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRavnica());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(battle.getId(), source.getId()));

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    void recipientMustBeDifferentFromTheSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void choosingZeroSourcesDoesNotBoostOrDamageTheRecipient() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(victim.getId()));

        assertThat(victim.getEffectivePower()).isEqualTo(3);
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tandem Takedown");
    }

    @Test
    void canDamageAPlaneswalkerYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new WrennAndRealmbreaker());
        victim.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(victim.getId(), source.getId()));

        assertThat(victim.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(source.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void remainingSourceDealsDamageWhenTheOtherSourceLeaves() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castInstant(player1, 0, List.of(victim.getId(), firstSource.getId(), secondSource.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(firstSource);
        harness.passBothPriorities();

        assertThat(secondSource.getEffectivePower()).isEqualTo(3);
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void sourceThatChangesControllerIsNotBoostedAndDoesNotDealDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castInstant(player1, 0, List.of(victim.getId(), source.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(victim.getEffectivePower()).isEqualTo(4);
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    void sourcesAreStillBoostedWhenTheRecipientLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castInstant(player1, 0, List.of(victim.getId(), source.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(victim.getId(), source.getId()));
        assertThat(source.getEffectivePower()).isEqualTo(3);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(source.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void cannotChooseAnOpponentsCreatureAsASource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(victim.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotDamageAPlayer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
