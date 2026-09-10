package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThalakosSentry.class, CanopySpider.class})
class ThalakosSentryTest extends BaseCardTest {

    @Test
    @DisplayName("A non-shadow creature cannot block Thalakos Sentry")
    void cannotBeBlockedByNonShadowCreature() {
        addAttacker(new ThalakosSentry());
        addCreatureReady(player2, new CanopySpider());
        prepareDeclareBlockers();

        assertThatThrownBy(this::declareSingleBlocker)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Thalakos Sentry cannot block a non-shadow creature")
    void cannotBlockNonShadowCreature() {
        addAttacker(new CanopySpider());
        addCreatureReady(player2, new ThalakosSentry());
        prepareDeclareBlockers();

        assertThatThrownBy(this::declareSingleBlocker)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A shadow creature can block Thalakos Sentry")
    void canBeBlockedByShadowCreature() {
        addAttacker(new ThalakosSentry());
        Permanent blocker = addCreatureReady(player2, new ThalakosSentry());
        prepareDeclareBlockers();

        declareSingleBlocker();

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareSingleBlocker() {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
