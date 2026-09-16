package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EvergloveCourier;
import com.github.laxika.magicalvibes.cards.k.KrosanTusker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiptideBiologist.class, KrosanTusker.class, EvergloveCourier.class})
class RiptideBiologistTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new RiptideBiologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent biologist = findPermanent(player1, "Riptide Biologist");
        assertThat(biologist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(biologist));
        harness.passBothPriorities();

        assertThat(biologist.isFaceDown()).isFalse();
    }

    @Test
    void cannotBeBlockedByBeastCreature() {
        Permanent biologist = addCreatureReady(player1, new RiptideBiologist());
        biologist.setAttacking(true);
        Permanent beast = addCreatureReady(player2, new KrosanTusker());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(beast),
                gd.playerBattlefields.get(player1.getId()).indexOf(biologist)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void canBeBlockedByNonBeastCreature() {
        Permanent biologist = addCreatureReady(player1, new RiptideBiologist());
        biologist.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new EvergloveCourier());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(biologist))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockBeastCreature() {
        Permanent attacker = addCreatureReady(player1, new KrosanTusker());
        attacker.setAttacking(true);

        Permanent biologist = addCreatureReady(player2, new RiptideBiologist());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(biologist),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void preventsCombatDamageFromAlreadyBlockedBeastCreature() {
        Permanent attacker = addCreatureReady(player1, new KrosanTusker());
        attacker.setAttacking(true);

        Permanent biologist = addCreatureReady(player2, new RiptideBiologist());
        biologist.setBlocking(true);
        biologist.addBlockingTarget(0);

        resolveCombat();

        assertThat(biologist.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(biologist);
    }
}
