package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VillageCannibalsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ANY_CREATURE_DIES effect with SubtypeConditionalEffect wrapping PutCountersOnSourceEffect")
    void hasCorrectStructure() {
        VillageCannibals card = new VillageCannibals();

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst())
                .isInstanceOf(SubtypeConditionalEffect.class);
        SubtypeConditionalEffect filtered =
                (SubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst();
        assertThat(filtered.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(filtered.wrapped()).isInstanceOf(PutCountersOnSourceEffect.class);
        PutCountersOnSourceEffect counter = (PutCountersOnSourceEffect) filtered.wrapped();
        assertThat(counter.powerModifier()).isEqualTo(1);
        assertThat(counter.toughnessModifier()).isEqualTo(1);
        assertThat(counter.amount()).isEqualTo(1);
    }

    // ===== Trigger: gets +1/+1 counter when another Human creature dies =====

    @Test
    @DisplayName("Gets a +1/+1 counter when an ally Human creature dies")
    void getsCounterWhenAllyHumanCreatureDies() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player1, new UnrulyMob()); // Human creature

        Permanent cannibals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Village Cannibals"))
                .findFirst().orElseThrow();
        assertThat(cannibals.getPlusOnePlusOneCounters()).isZero();

        // Kill ally Human creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID mobId = harness.getPermanentId(player1, "Unruly Mob");
        harness.castInstant(player2, 0, mobId);
        harness.passBothPriorities(); // Resolve Shock -> Unruly Mob dies -> death triggers
        harness.passBothPriorities(); // Resolve Village Cannibals' +1/+1 counter trigger

        assertThat(cannibals.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, cannibals)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cannibals)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's Human creature dies")
    void getsCounterWhenOpponentHumanCreatureDies() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player2, new UnrulyMob()); // Opponent's Human creature

        Permanent cannibals = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cannibals.getPlusOnePlusOneCounters()).isZero();

        // Kill opponent's Human creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mobId = harness.getPermanentId(player2, "Unruly Mob");
        harness.castInstant(player1, 0, mobId);
        harness.passBothPriorities(); // Resolve Shock -> Unruly Mob dies -> death trigger
        harness.passBothPriorities(); // Resolve Village Cannibals' +1/+1 counter trigger

        assertThat(cannibals.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does NOT get a counter when a non-Human creature dies")
    void doesNotGetCounterWhenNonHumanCreatureDies() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bear, not Human

        Permanent cannibals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Village Cannibals"))
                .findFirst().orElseThrow();
        assertThat(cannibals.getPlusOnePlusOneCounters()).isZero();

        // Kill non-Human creature with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock -> bears die

        // No trigger should have been added for Village Cannibals
        assertThat(cannibals.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Accumulates multiple +1/+1 counters as multiple Human creatures die")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addToBattlefield(player2, new UnrulyMob());

        Permanent cannibals = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Kill first Human creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mobId = harness.getPermanentId(player2, "Unruly Mob");
        harness.castInstant(player1, 0, mobId);
        harness.passBothPriorities(); // Resolve Shock -> Unruly Mob dies -> death trigger
        harness.passBothPriorities(); // Resolve Village Cannibals' trigger

        assertThat(cannibals.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Add and kill second Human creature
        harness.addToBattlefield(player2, new UnrulyMob());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID mob2Id = harness.getPermanentId(player2, "Unruly Mob");
        harness.castInstant(player1, 0, mob2Id);
        harness.passBothPriorities(); // Resolve Shock -> Unruly Mob dies -> death trigger
        harness.passBothPriorities(); // Resolve Village Cannibals' trigger

        assertThat(cannibals.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cannibals)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cannibals)).isEqualTo(4);
    }
}
