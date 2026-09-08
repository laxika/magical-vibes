package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EdgewallPack.class)
class EdgewallPackTest extends BaseCardTest {

    @Test
    void entersAndCreatesNonblockingRatToken() {
        harness.setHand(player1, List.of(new EdgewallPack()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(rats.getFirst().getCard().isToken()).isTrue();
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }
}
