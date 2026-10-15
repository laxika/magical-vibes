package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AwakenTheWoods.class})
class AwakenTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 green Forest Dryad land creature tokens")
    void createsForestDryads() {
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castAndResolveSorcery(player1, 0, 3);

        List<Permanent> dryads = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest Dryad"))
                .toList();
        assertThat(dryads).hasSize(3);
        assertThat(dryads).allSatisfy(dryad -> {
            assertThat(gqs.isCreature(gd, dryad)).isTrue();
            assertThat(gqs.isLand(gd, dryad)).isTrue();
            assertThat(dryad.getEffectivePower()).isEqualTo(1);
            assertThat(dryad.getEffectiveToughness()).isEqualTo(1);
            assertThat(dryad.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.DRYAD);
            assertThat(dryad.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(dryad.getCard().getAdditionalTypes()).containsExactly(CardType.LAND);
            assertThat(dryad.isSummoningSick()).isTrue();
            assertThat(dryad.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With X=0, creates no tokens")
    void createsNoTokensForZero() {
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest Dryad")))
                .isEmpty();
    }

    @Test
    @DisplayName("Forest Dryad tokens can tap for green mana after summoning sickness ends")
    void tokensProduceGreenMana() {
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 1);

        Permanent dryad = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(dryad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("New Forest Dryad tokens cannot tap for mana while summoning sick")
    void newTokensCannotTapForMana() {
        harness.setHand(player1, List.of(new AwakenTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
