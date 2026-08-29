package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HopToIt.class})
class HopToItTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Rabbit tokens")
    void createsThreeRabbitTokens() {
        harness.setHand(player1, List.of(new HopToIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.RABBIT);
                    assertThat(permanent.getEffectivePower()).isEqualTo(1);
                    assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
                });
    }
}
