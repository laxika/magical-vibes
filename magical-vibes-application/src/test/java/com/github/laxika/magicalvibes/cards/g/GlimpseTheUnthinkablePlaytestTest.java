package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PathOfPeace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheUnthinkablePlaytest.class, PathOfPeace.class})
class GlimpseTheUnthinkablePlaytestTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Glimpse, the Unthinkable")
    void shroudPreventsTargeting() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(
                player1, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player2, List.of(new PathOfPeace()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, glimpse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
