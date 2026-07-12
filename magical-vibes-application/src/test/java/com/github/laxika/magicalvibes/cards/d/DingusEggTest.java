package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DingusEggTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the land's controller when their land is destroyed")
    void dealsToLandControllerWhenLandDestroyed() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player2, 20);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, mountainId);
        harness.passBothPriorities(); // Resolve Stone Rain — Mountain dies

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dingus Egg");

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to controller when their own land is destroyed")
    void dealsToSelfWhenOwnLandDestroyed() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, mountainId);
        harness.passBothPriorities(); // Resolve Stone Rain

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dingus Egg");

        harness.passBothPriorities(); // Resolve trigger

        // Player1 controlled the land, so player1 takes the 2 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when a non-land permanent dies")
    void doesNotTriggerOnNonLand() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict — Grizzly Bears dies

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Dingus Eggs each trigger when a land is destroyed")
    void twoEggsEachTrigger() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player2, 20);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, mountainId);
        harness.passBothPriorities(); // Resolve Stone Rain

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(se -> se.getCard().getName().equals("Dingus Egg"));

        harness.passBothPriorities(); // Resolve first trigger
        harness.passBothPriorities(); // Resolve second trigger

        // 2 + 2 = 4 damage total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Trigger is logged when it fires")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new DingusEgg());
        harness.addToBattlefield(player2, new Mountain());

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, mountainId);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Dingus Egg") && log.contains("triggers"));
    }
}
