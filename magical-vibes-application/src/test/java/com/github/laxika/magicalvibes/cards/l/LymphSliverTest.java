package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArcBlade;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.FrenzySliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LymphSliver.class, FrenzySliver.class, ArcBlade.class, BlindPhantasm.class})
class LymphSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Absorb 1 prevents one damage from each damage event to every Sliver")
    void preventsOneDamageFromEachDamageEventToEverySliver() {
        Permanent sourceSliver = addCreatureReady(player1, new LymphSliver());
        Permanent opposingSliver = addCreatureReady(player2, new FrenzySliver());
        Permanent nonSliver = addCreatureReady(player2, new BlindPhantasm());

        harness.setHand(player1, List.of(new ArcBlade(), new ArcBlade(), new ArcBlade(), new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 20);

        harness.castSorcery(player1, 0, sourceSliver.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, sourceSliver.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, opposingSliver.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, nonSliver.getId());
        harness.passBothPriorities();

        assertThat(sourceSliver.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingSliver.getMarkedDamage()).isEqualTo(1);
        assertThat(nonSliver.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Absorb 1 prevents one combat damage to a Sliver")
    void preventsCombatDamageToSliver() {
        Permanent attacker = addCreatureReady(player1, new BlindPhantasm());
        Permanent blocker = addCreatureReady(player2, new LymphSliver());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Lymph Slivers grant separate instances of absorb")
    void multipleInstancesPreventDamageSeparately() {
        Permanent first = addCreatureReady(player1, new LymphSliver());
        Permanent second = addCreatureReady(player2, new LymphSliver());
        Permanent otherSliver = addCreatureReady(player2, new FrenzySliver());
        harness.setHand(player1, List.of(new ArcBlade(), new ArcBlade(), new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 15);

        harness.castSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, otherSliver.getId());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(otherSliver.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Frenzy Sliver");
    }

    @Test
    @DisplayName("Slivers lose absorb when Lymph Sliver leaves the battlefield")
    void losesAbsorbWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new LymphSliver());
        Permanent otherSliver = addCreatureReady(player2, new FrenzySliver());
        harness.setHand(player1, List.of(new ArcBlade(), new ArcBlade(), new ArcBlade(), new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 20);

        for (int i = 0; i < 3; i++) {
            harness.castSorcery(player1, 0, source.getId());
            harness.passBothPriorities();
        }
        harness.assertInGraveyard(player1, "Lymph Sliver");

        harness.castSorcery(player1, 0, otherSliver.getId());
        harness.passBothPriorities();

        assertThat(otherSliver.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Frenzy Sliver");
    }
}
