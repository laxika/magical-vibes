package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class RubblebeltBoarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2.getId(), targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1.getId(), targetId);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2.getId(), targetId);
        assertThat(bears.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can be cast with no creatures around")
    void castWithoutTarget() {
        harness.setHand(player1, List.of(new RubblebeltBoar()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rubblebelt Boar");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findPermanent(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getId().equals(permanentId))
                .findFirst().orElseThrow();
    }
}
