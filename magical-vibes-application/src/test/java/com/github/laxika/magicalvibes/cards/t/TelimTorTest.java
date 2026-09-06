package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TelimTor.class, MtendaHerder.class, FemerefScouts.class})
class TelimTorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Telim'Tor gives +1/+1 to every attacking creature with flanking")
    void boostsAttackingFlankers() {
        addCreatureReady(player1, new TelimTor());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new FemerefScouts());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        Permanent telimTor = findPermanent(player1, "Telim'Tor");
        Permanent herder = findPermanent(player1, "Mtenda Herder");
        Permanent scouts = findPermanent(player1, "Femeref Scouts");

        assertThat(gqs.getEffectivePower(gd, telimTor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, telimTor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(4);
    }

    @Test
    @DisplayName("A flanking creature that stays home is not boosted")
    void doesNotBoostNonAttackingFlankers() {
        addCreatureReady(player1, new TelimTor());
        addCreatureReady(player1, new MtendaHerder());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent herder = findPermanent(player1, "Mtenda Herder");
        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(1);
    }

    @Test
    @DisplayName("Telim'Tor's flanking weakens a non-flanking blocker")
    void flankingWeakensNonFlankingBlocker() {
        Permanent telimTor = addCreatureReady(player1, new TelimTor());
        telimTor.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Telim'Tor's attack ability does not trigger when it stays home")
    void doesNotTriggerWhenTelimTorDoesNotAttack() {
        addCreatureReady(player1, new TelimTor());
        Permanent herder = addCreatureReady(player1, new MtendaHerder());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(1);
    }

    @Test
    @DisplayName("Telim'Tor's attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TelimTor());
        Permanent herder = addCreatureReady(player1, new MtendaHerder());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(1);
    }
}
