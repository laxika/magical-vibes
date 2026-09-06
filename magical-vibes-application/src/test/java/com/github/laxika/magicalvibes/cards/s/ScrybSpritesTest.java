package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrybSprites.class, GrizzlyBears.class})
class ScrybSpritesTest extends BaseCardTest {

    @Test
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new ScrybSprites());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockNonFlyingCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new ScrybSprites());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);

        assertThatCode(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
