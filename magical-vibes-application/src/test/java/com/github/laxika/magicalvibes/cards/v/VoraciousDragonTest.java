package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoraciousDragonTest extends BaseCardTest {

    private void castVoraciousDragon(java.util.UUID targetId) {
        harness.setLife(player2, 20);
        harness.setHand(player1, new ArrayList<>(List.of(new VoraciousDragon())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Permanent dragon() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Voracious Dragon"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Devouring two Goblins deals twice their number (4) to any target")
    void devourTwoGoblinsDealsFour() {
        Permanent goblinA = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent goblinB = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(goblinA.getId(), goblinB.getId()));

        // Devour 1 x 2 creatures = 2 +1/+1 counters.
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities(); // resolve the ETB damage trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 20 - (2 x 2 Goblins)
    }

    @Test
    @DisplayName("Only Goblins devoured count for the damage; a devoured non-Goblin adds a counter but no damage")
    void onlyGoblinsCountForDamage() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        harness.handleMultiplePermanentsChosen(player1, List.of(goblin.getId(), bear.getId()));

        // Both devoured creatures grant counters...
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities(); // resolve the ETB damage trigger

        // ...but only the single Goblin counts for damage: 2 x 1 = 2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining to devour deals no damage even with a Goblin available")
    void declineDevourDealsNoDamage() {
        harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        castVoraciousDragon(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve the ETB damage trigger (0 damage)

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
