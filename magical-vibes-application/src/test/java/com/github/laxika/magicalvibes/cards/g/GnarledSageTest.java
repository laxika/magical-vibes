package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GnarledSageTest extends BaseCardTest {

    @Test
    @DisplayName("Gnarled Sage gets +0/+2 and vigilance after its controller draws two cards")
    void gainsBonusAfterControllerDrawsTwoCards() {
        Permanent sage = addCreatureReady(player1, new GnarledSage());
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);

        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();

        draw(player1);

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's draws do not enable Gnarled Sage")
    void opponentDrawsDoNotEnableBonus() {
        Permanent sage = addCreatureReady(player1, new GnarledSage());

        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        draw(player2);
        draw(player2);

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, sage, Keyword.VIGILANCE)).isFalse();
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
