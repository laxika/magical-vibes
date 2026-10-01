package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArcBlade;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.FrenzySliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }
}
