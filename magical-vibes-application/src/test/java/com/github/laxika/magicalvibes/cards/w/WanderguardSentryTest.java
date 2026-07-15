package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WanderguardSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castWanderguardSentry(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wanderguard Sentry"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger reveals target opponent's hand to the controller")
    void etbRevealsOpponentHand() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castWanderguardSentry(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB trigger against empty hand logs that the hand is empty")
    void etbEmptyHandLogged() {
        harness.setHand(player2, List.of());
        castWanderguardSentry(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new WanderguardSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castWanderguardSentry(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new WanderguardSentry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
