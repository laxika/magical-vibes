package com.github.laxika.magicalvibes.cards.u;

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

class UnrulyMobTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ALLY_CREATURE_DIES effect with PutCountersOnSourceEffect")
    void hasCorrectStructure() {
        UnrulyMob card = new UnrulyMob();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst())
                .isInstanceOf(PutCountersOnSourceEffect.class);
        PutCountersOnSourceEffect effect =
                (PutCountersOnSourceEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst();
        assertThat(effect.powerModifier()).isEqualTo(1);
        assertThat(effect.toughnessModifier()).isEqualTo(1);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== ON_ALLY_CREATURE_DIES: gets +1/+1 counter when an ally creature dies =====

    @Test
    @DisplayName("Gets a +1/+1 counter when an ally creature dies")
    void getsCounterWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent unrulyMob = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Unruly Mob"))
                .findFirst().orElseThrow();
        assertThat(unrulyMob.getPlusOnePlusOneCounters()).isZero();

        // Kill ally creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Unruly Mob's +1/+1 counter trigger

        assertThat(unrulyMob.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, unrulyMob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unrulyMob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does NOT get a counter when an opponent's creature dies")
    void doesNotGetCounterWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent unrulyMob = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(unrulyMob.getPlusOnePlusOneCounters()).isZero();

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die

        // No trigger should have been added for Unruly Mob
        assertThat(unrulyMob.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Accumulates multiple +1/+1 counters as multiple ally creatures die")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new UnrulyMob());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent unrulyMob = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Unruly Mob"))
                .findFirst().orElseThrow();

        // Kill first ally creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Unruly Mob's trigger

        assertThat(unrulyMob.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Add and kill second ally creature
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bears2Id = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bears2Id);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve Unruly Mob's trigger

        assertThat(unrulyMob.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unrulyMob)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, unrulyMob)).isEqualTo(3);
    }
}
