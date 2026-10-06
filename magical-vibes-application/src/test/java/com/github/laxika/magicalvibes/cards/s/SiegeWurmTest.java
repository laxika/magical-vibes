package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiegeWurm.class, ElvesOfDeepShadow.class})
class SiegeWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new SiegeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Siege Wurm")).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke the entire spell cost without activating mana abilities")
    void castsEntirelyWithSummoningSickCreatures() {
        List<Permanent> creatures = IntStream.range(0, 7)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SiegeWurm()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Siege Wurm");
    }

    @Test
    @DisplayName("An already tapped creature cannot convoke")
    void cannotConvokeWithTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new SiegeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Siege Wurm");
        harness.assertNotOnBattlefield(player1, "Siege Wurm");
    }

    @Test
    @DisplayName("Trample requires lethal damage to every blocker before damaging the player")
    void cannotTramplePastASecondBlockerWithoutLethalDamage() {
        Permanent wurm = addCreatureReady(player1, new SiegeWurm());
        wurm.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new ElvesOfDeepShadow());
        Permanent secondBlocker = addCreatureReady(player2, new ElvesOfDeepShadow());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 0,
                player2.getId(), 4
        ))).isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player2, "Elves of Deep Shadow")).isZero();
        harness.assertOnBattlefield(player1, "Siege Wurm");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);

        Permanent wurm = addCreatureReady(player1, new SiegeWurm());
        wurm.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ElvesOfDeepShadow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Elves of Deep Shadow");
    }
}
