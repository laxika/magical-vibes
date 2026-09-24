package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(OrnateImitations.class)
class OrnateImitationsTest extends BaseCardTest {

    @Test
    void conjuresOneRandomCreatureForEachManaValueThroughX() {
        harness.setHand(player1, List.of(new OrnateImitations()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 2);

        List<Permanent> conjured = gd.playerBattlefields.get(player1.getId());
        assertThat(conjured).hasSize(2);
        assertThat(conjured).allSatisfy(permanent -> {
            assertThat(permanent.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(permanent.getCard().isToken()).isFalse();
        });
        assertThat(conjured.stream().map(permanent -> permanent.getCard().getManaValue()).toList())
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void cannotBeCastWithZeroX() {
        harness.setHand(player1, List.of(new OrnateImitations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
