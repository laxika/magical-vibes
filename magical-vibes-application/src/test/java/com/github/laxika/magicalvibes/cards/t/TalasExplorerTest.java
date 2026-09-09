package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.l.Lynx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalasExplorer.class, Lynx.class})
class TalasExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castTalasExplorer(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Talas Explorer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger looks at target opponent's hand")
    void etbLooksAtTargetHand() {
        harness.setHand(player2, List.of(new Lynx()));
        castTalasExplorer(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Card identity is private: only the controller is told what is in the hand. The public log
        // records that the look happened without naming anything (see CardRevealService#lookAtHand).
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Lynx"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Lynx"));
    }

    @Test
    @DisplayName("ETB trigger against empty hand logs that hand is empty")
    void etbEmptyHandLogged() {
        harness.setHand(player2, List.of());
        castTalasExplorer(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Cannot target self because self is not an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new TalasExplorer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTalasExplorer(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new TalasExplorer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
