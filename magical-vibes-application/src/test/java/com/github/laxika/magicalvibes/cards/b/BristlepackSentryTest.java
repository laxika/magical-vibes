package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BristlepackSentry.class, EnormousBaloth.class})
class BristlepackSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Bristlepack Sentry cannot attack without a creature with power 4 or greater")
    void cannotAttackWithoutLargeCreature() {
        addCreatureReady(player1, new BristlepackSentry());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bristlepack Sentry can attack while its controller controls a creature with power 4 or greater")
    void canAttackWithLargeCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BristlepackSentry());
        addCreatureReady(player1, new EnormousBaloth());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Bristlepack Sentry does not count a large creature controlled by an opponent")
    void doesNotCountOpponentsLargeCreature() {
        addCreatureReady(player1, new BristlepackSentry());
        addCreatureReady(player2, new EnormousBaloth());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
