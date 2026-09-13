package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({PlatedSpider.class, AirElemental.class})
class PlatedSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Plated Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player2, new PlatedSpider());
        addCreatureReady(player1, new AirElemental());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
