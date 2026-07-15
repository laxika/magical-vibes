package com.github.laxika.magicalvibes.cards.w;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WuScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with targeted opponent")
    void resolvingPutsEtbOnStackWithTarget() {
        castWuScout(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wu Scout"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger looks at target opponent's hand")
    void etbLooksAtTargetHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castWuScout(player2.getId());

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target self — self is not an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new WuScout()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWuScout(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new WuScout()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
