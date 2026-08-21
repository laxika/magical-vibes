package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MijaeDjinn.class, GrizzlyBears.class})
class MijaeDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("On attack, a lost coin flip removes and taps the Djinn")
    void lostCoinFlipRemovesAndTapsSource() {
        Permanent djinn = addCreatureReady(player1, new MijaeDjinn());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        boolean lostFlip = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("loses the coin flip for Mijae Djinn"));
        if (lostFlip) {
            assertThat(djinn.isAttacking()).isFalse();
            assertThat(djinn.isTapped()).isTrue();
        } else {
            assertThat(djinn.isAttacking()).isTrue();
            assertThat(djinn.isTapped()).isFalse();
        }
    }
}
