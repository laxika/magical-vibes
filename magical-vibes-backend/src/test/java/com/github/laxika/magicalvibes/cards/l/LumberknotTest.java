package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LumberknotTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ANY_CREATURE_DIES effect with PutCountersOnSourceEffect")
    void hasCorrectStructure() {
        Lumberknot card = new Lumberknot();

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst())
                .isInstanceOf(PutCountersOnSourceEffect.class);
        PutCountersOnSourceEffect effect =
                (PutCountersOnSourceEffect) card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst();
        assertThat(effect.powerModifier()).isEqualTo(1);
        assertThat(effect.toughnessModifier()).isEqualTo(1);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== ON_ANY_CREATURE_DIES: gets +1/+1 counter when a creature dies =====

    @Test
    @DisplayName("Gets a +1/+1 counter when an ally creature dies")
    void getsCounterWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new Lumberknot());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent lumberknot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lumberknot"))
                .findFirst().orElseThrow();
        assertThat(lumberknot.getPlusOnePlusOneCounters()).isZero();

        // Kill ally creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's +1/+1 counter trigger

        assertThat(lumberknot.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lumberknot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lumberknot)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature dies")
    void getsCounterWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new Lumberknot());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent lumberknot = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(lumberknot.getPlusOnePlusOneCounters()).isZero();

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's +1/+1 counter trigger

        assertThat(lumberknot.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accumulates multiple +1/+1 counters as multiple creatures die")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new Lumberknot());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent lumberknot = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Kill first creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's trigger

        assertThat(lumberknot.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Add and kill second creature
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bears2Id = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bears2Id);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Lumberknot's trigger

        assertThat(lumberknot.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lumberknot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lumberknot)).isEqualTo(3);
    }
}
