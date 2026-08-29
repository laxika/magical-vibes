package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaverickThopteristTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 colorless Thopter artifact creature tokens with flying")
    void etbCreatesTwoThopters() {
        harness.setHand(player1, List.of(new MaverickThopterist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
    }
}
