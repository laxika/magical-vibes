package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChokingFumesTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has correct effect and does not need a target")
    void hasCorrectProperties() {
        ChokingFumes card = new ChokingFumes();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).singleElement()
                .isInstanceOf(PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect.class);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Puts a -1/-1 counter on each attacking creature")
    void putsCounterOnEachAttackingCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        bears2.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(bears2.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect non-attacking creatures")
    void doesNotAffectNonAttackingCreatures() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        Permanent nonAttacker = new Permanent(new GrizzlyBears());
        nonAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(nonAttacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(attacker.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(nonAttacker.getMinusOneMinusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kills a 1/1 attacking creature with the -1/-1 counter")
    void killsOneOneAttacker() {
        Permanent elf = new Permanent(new LlanowarElves());
        elf.setSummoningSick(false);
        elf.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(elf);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Affects attacking creatures from both players")
    void affectsAttackersFromBothPlayers() {
        // Player 2's attacker
        Permanent p2Attacker = new Permanent(new GrizzlyBears());
        p2Attacker.setSummoningSick(false);
        p2Attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(p2Attacker);

        // Player 1's creature that is also attacking (e.g. from a previous combat or extra combat)
        Permanent p1Attacker = new Permanent(new GrizzlyBears());
        p1Attacker.setSummoningSick(false);
        p1Attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(p1Attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Both attacking creatures get counters
        assertThat(p2Attacker.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(p1Attacker.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when no creatures are attacking")
    void doesNothingWithNoAttackers() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ChokingFumes()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Non-attacking creature is unaffected
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(0);
    }
}
