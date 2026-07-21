package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianOfPilgrimsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +1/+1 until end of turn")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target its own controller's creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bearsId, null);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB → fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
