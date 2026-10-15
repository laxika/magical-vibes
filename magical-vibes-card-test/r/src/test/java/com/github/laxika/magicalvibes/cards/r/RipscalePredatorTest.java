package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RipscalePredator.class, ArmoredTransport.class})
class RipscalePredatorTest extends BaseCardTest {

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new RipscalePredator());
        addCreatureReady(player2, new ArmoredTransport());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new RipscalePredator());
        Permanent first = addCreatureReady(player2, new ArmoredTransport());
        Permanent second = addCreatureReady(player2, new ArmoredTransport());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void menaceAllowsMoreThanTwoBlockers() {
        addCreatureReady(player1, new RipscalePredator());
        Permanent first = addCreatureReady(player2, new ArmoredTransport());
        Permanent second = addCreatureReady(player2, new ArmoredTransport());
        Permanent third = addCreatureReady(player2, new ArmoredTransport());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0), new BlockerAssignment(2, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
        assertThat(third.isBlocking()).isTrue();
    }

    @Test
    void menaceDoesNotRequireDefenderToBlock() {
        addCreatureReady(player1, new RipscalePredator());
        addCreatureReady(player2, new ArmoredTransport());
        addCreatureReady(player2, new ArmoredTransport());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }
}
