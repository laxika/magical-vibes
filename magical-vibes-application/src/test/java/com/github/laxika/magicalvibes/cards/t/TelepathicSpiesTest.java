package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AetherSting;
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

@CardUsed({TelepathicSpies.class, AetherSting.class})
class TelepathicSpiesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castTelepathicSpies(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Telepathic Spies");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger looks at target opponent's hand")
    void etbLooksAtTargetHand() {
        harness.setHand(player2, List.of(new AetherSting()));
        castTelepathicSpies(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Card identity is private: only the controller is told what is in the hand. The public log
        // records that the look happened without naming anything (see CardRevealService#lookAtHand).
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Aether Sting"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Aether Sting"));
    }

    @Test
    @DisplayName("ETB trigger against empty hand logs that hand is empty")
    void etbEmptyHandLogged() {
        harness.setHand(player2, List.of());
        castTelepathicSpies(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Cannot target self because self is not an opponent")
    void cannotTargetSelf() {
        assertThatThrownBy(() -> castTelepathicSpies(player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTelepathicSpies(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new TelepathicSpies()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
