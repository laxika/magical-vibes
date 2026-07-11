package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrighthearthBanneretTest extends BaseCardTest {

    // ===== Elemental / Warrior cost reduction =====

    @Test
    @DisplayName("Elemental spells cost {1} less to cast with Brighthearth Banneret on the battlefield")
    void elementalSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Smokebraider costs {1}{R} — with {1} reduction it should cost just {R}
        harness.setHand(player1, List.of(new Smokebraider()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Smokebraider");
    }

    @Test
    @DisplayName("Warrior spells cost {1} less to cast with Brighthearth Banneret on the battlefield")
    void warriorSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Boldwyr Intimidator (Giant Warrior) costs {5}{R}{R} — with {1} reduction it costs {4}{R}{R} = 6 mana
        harness.setHand(player1, List.of(new BoldwyrIntimidator()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Boldwyr Intimidator");
    }

    @Test
    @DisplayName("Non-Elemental/Warrior spells are not reduced by Brighthearth Banneret")
    void otherSpellsNotReduced() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Grizzly Bears (Bear) costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Brighthearth Banneret does not reduce an opponent's Elemental spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Opponent's Smokebraider should still cost {1}{R}
        harness.setHand(player2, List.of(new Smokebraider()));
        harness.addMana(player2, ManaColor.RED, 1);

        // Only {R} is not enough for {1}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Reinforce 1—{1}{R} =====

    @Test
    @DisplayName("Reinforce puts a +1/+1 counter on target creature")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce discards the source card to the graveyard as a cost")
    void reinforceDiscardsSourceCard() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, bears.getId());

        harness.assertNotInHand(player1, "Brighthearth Banneret");
        harness.assertInGraveyard(player1, "Brighthearth Banneret");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature permanent; no cost is paid")
    void reinforceRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
