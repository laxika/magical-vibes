package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dogs you control get +1/+1")
    void buffsOtherDogsYouControl() {
        Permanent packLeader = harness.addToBattlefieldAndReturn(player1, new PackLeader());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        Permanent opponentDog = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, packLeader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, packLeader)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentDog)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentDog)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking prevents combat damage to Dogs you control")
    void preventsCombatDamageToControlledDogs() {
        Permanent packLeader = addCreatureReady(player1, new PackLeader());
        Permanent dog = addCreatureReady(player1, new AlpineWatchdog());
        Permanent packBlocker = addCreatureReady(player2, creatureWithStats(4, 4));
        Permanent dogBlocker = addCreatureReady(player2, creatureWithStats(4, 4));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(packBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(packLeader)),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(dogBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(dog))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(packLeader, dog);
        assertThat(packLeader.getMarkedDamage()).isZero();
        assertThat(dog.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attack trigger does not protect an opponent's Dog")
    void doesNotPreventCombatDamageToOpponentsDogs() {
        Permanent packLeader = addCreatureReady(player1, new PackLeader());
        Permanent opponentDog = addCreatureReady(player2, new AlpineWatchdog());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(opponentDog),
                gd.playerBattlefields.get(player1.getId()).indexOf(packLeader))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpine Watchdog");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(packLeader);
        assertThat(packLeader.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attack trigger prevents combat damage only")
    void doesNotPreventNoncombatDamage() {
        Permanent packLeader = addCreatureReady(player1, new PackLeader());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, packLeader.getId());
        harness.passBothPriorities();

        assertThat(packLeader.getMarkedDamage()).isEqualTo(2);
    }

    private Card creatureWithStats(int power, int toughness) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
