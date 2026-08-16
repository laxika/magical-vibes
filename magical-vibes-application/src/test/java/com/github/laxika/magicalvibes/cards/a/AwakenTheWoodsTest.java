package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AwakenTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 green Forest Dryad land creature tokens")
    void createsForestDryads() {
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        List<Permanent> dryads = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest Dryad"))
                .toList();
        assertThat(dryads).hasSize(3);
        assertThat(dryads).allSatisfy(dryad -> {
            assertThat(gqs.isCreature(gd, dryad)).isTrue();
            assertThat(gqs.isLand(gd, dryad)).isTrue();
            assertThat(dryad.getEffectivePower()).isEqualTo(1);
            assertThat(dryad.getEffectiveToughness()).isEqualTo(1);
            assertThat(dryad.getCard().getSubtypes()).containsExactly(CardSubtype.DRYAD);
            assertThat(dryad.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(dryad.getCard().getAdditionalTypes()).containsExactly(CardType.LAND);
            assertThat(dryad.isSummoningSick()).isTrue();
        });
    }

    @Test
    @DisplayName("With X=0, creates no tokens")
    void createsNoTokensForZero() {
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest Dryad")))
                .isEmpty();
    }
}
