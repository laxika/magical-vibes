package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValorousSteedTest extends BaseCardTest {

    @Test
    @DisplayName("When Valorous Steed enters, it creates a 2/2 Knight token with vigilance")
    void etbCreatesKnightTokenWithVigilance() {
        harness.setHand(player1, List.of(new ValorousSteed()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Knight");
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }
}
