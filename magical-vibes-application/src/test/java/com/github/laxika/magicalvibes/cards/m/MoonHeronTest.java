package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.Kindercatch;
import com.github.laxika.magicalvibes.cards.s.SomberwaldSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonHeron.class, Kindercatch.class, SomberwaldSpider.class})
class MoonHeronTest extends BaseCardTest {

    @Test
    void nonflyingCreatureCannotBlock() {
        addCreatureReady(player1, new MoonHeron());
        addCreatureReady(player2, new Kindercatch());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new MoonHeron());
        Permanent blocker = addCreatureReady(player2, new MoonHeron());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new MoonHeron());
        Permanent blocker = addCreatureReady(player2, new SomberwaldSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockNonflyingCreature() {
        addCreatureReady(player1, new Kindercatch());
        Permanent blocker = addCreatureReady(player2, new MoonHeron());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
