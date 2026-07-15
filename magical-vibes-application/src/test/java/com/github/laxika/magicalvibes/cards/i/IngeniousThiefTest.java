package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IngeniousThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected target")
    void resolvingPutsEtbOnStackWithTarget() {
        castIngeniousThief(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ingenious Thief"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger looks at target player's hand")
    void etbLooksAtTargetHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castIngeniousThief(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB trigger against empty hand logs that hand is empty")
    void etbEmptyHandLogged() {
        harness.setHand(player2, new ArrayList<>());
        castIngeniousThief(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Can target self to look at own hand")
    void canTargetSelf() {
        castIngeniousThief(player1.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    private void castIngeniousThief(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new IngeniousThief()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
