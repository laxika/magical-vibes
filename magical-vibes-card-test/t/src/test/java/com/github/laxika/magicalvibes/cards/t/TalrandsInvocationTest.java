package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TalrandsInvocation.class})
class TalrandsInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 2/2 blue Drake tokens with flying")
    void createsTwoFlyingDrakes() {
        harness.setHand(player1, List.of(new TalrandsInvocation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

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

    @Test
    @DisplayName("Drakes enter only on resolution under the caster's control")
    void tokensEnterOnlyOnResolutionWithOracleCharacteristics() {
        harness.setHand(player1, List.of(new TalrandsInvocation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(drake -> {
            assertThat(drake.getCard().isToken()).isTrue();
            assertThat(drake.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(drake.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(drake.getCard().getSubtypes()).containsExactly(CardSubtype.DRAKE);
            assertThat(drake.isTapped()).isFalse();
            assertThat(drake.isSummoningSick()).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Talrand's Invocation");
    }
}
