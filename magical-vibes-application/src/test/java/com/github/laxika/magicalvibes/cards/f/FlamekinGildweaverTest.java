package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlamekinGildweaverTest extends BaseCardTest {

    @Test
    @DisplayName("When Flamekin Gildweaver enters, it creates one Treasure token")
    void etbCreatesOneTreasureToken() {
        harness.setHand(player1, List.of(new FlamekinGildweaver()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().getCard().isToken()).isTrue();
        assertThat(treasures.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasures.getFirst().getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }
}
