package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TalrandsInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 2/2 blue Drake tokens with flying")
    void createsTwoFlyingDrakes() {
        harness.setHand(player1, List.of(new TalrandsInvocation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> drakes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Drake".equals(p.getCard().getName()))
                .toList();

        assertThat(drakes).hasSize(2);
        assertThat(drakes).allSatisfy(drake -> {
            assertThat(drake.getEffectivePower()).isEqualTo(2);
            assertThat(drake.getEffectiveToughness()).isEqualTo(2);
            assertThat(drake.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }
}
