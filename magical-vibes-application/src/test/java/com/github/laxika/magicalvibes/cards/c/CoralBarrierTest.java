package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoralBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("When Coral Barrier enters, it creates an islandwalking Squid token")
    void etbCreatesIslandwalkingSquidToken() {
        harness.setHand(player1, List.of(new CoralBarrier()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squids = findPermanents(player1, "Squid");
        assertThat(squids).hasSize(1);
        assertThat(gqs.hasKeyword(gd, squids.getFirst(), Keyword.ISLANDWALK)).isTrue();
    }
}
