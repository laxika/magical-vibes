package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.s.ScreechingGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConclaveEquenaut.class, BorosSwiftblade.class, ScreechingGriffin.class})
class ConclaveEquenautTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps a creature to help pay the cost")
    void castsWithConvoke() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        harness.setHand(player1, List.of(new ConclaveEquenaut()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Conclave Equenaut")).isEqualTo(1);
    }

    @Test
    @DisplayName("Convoke can pay one of the creature's colors")
    void convokeCanPayColoredMana() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        harness.setHand(player1, List.of(new ConclaveEquenaut()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Conclave Equenaut")).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning-sick creatures can convoke the entire cost without mana")
    void castsEntirelyWithSummoningSickCreatures() {
        List<Permanent> convokers = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade()))
                .toList();
        convokers.forEach(permanent -> permanent.setSummoningSick(true));
        harness.setHand(player1, List.of(new ConclaveEquenaut()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                convokers.stream().map(Permanent::getId).toList());

        assertThat(convokers).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Conclave Equenaut")).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped creature cannot convoke")
    void cannotConvokeWithTappedCreature() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        convoker.tap();
        harness.setHand(player1, List.of(new ConclaveEquenaut()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Conclave Equenaut");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature cannot convoke")
    void cannotConvokeWithOpponentsCreature() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player2, new BorosSwiftblade());
        harness.setHand(player1, List.of(new ConclaveEquenaut()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player1, "Conclave Equenaut");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cannot convoke twice for the same spell")
    void cannotConvokeWithDuplicateCreature() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        harness.setHand(player1, List.of(new ConclaveEquenaut()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId(), convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player1, "Conclave Equenaut");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent attacker = addReadyAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new BorosSwiftblade());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        Permanent attacker = addReadyAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new ScreechingGriffin());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new ConclaveEquenaut());
        attacker.setAttacking(true);
        return attacker;
    }
}
