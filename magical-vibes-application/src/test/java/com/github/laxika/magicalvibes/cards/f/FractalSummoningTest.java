package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FractalSummoning.class})
class FractalSummoningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with X +1/+1 counters")
    void createsFractalWithXCounters() {
        harness.setHand(player1, List.of(new FractalSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 3);

        Permanent fractal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A Fractal created with X=0 dies after resolution")
    void zeroCountersTokenDies() {
        harness.setHand(player1, List.of(new FractalSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Fractal Summoning");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.isToken());
    }

    @Test
    @DisplayName("Can pay both hybrid symbols with blue mana")
    void createsFractalWithBlueMana() {
        harness.setHand(player1, List.of(new FractalSummoning()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(fractal -> {
            assertThat(fractal.getCard().isToken()).isTrue();
            assertThat(fractal.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(fractal.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
            assertThat(fractal.getCard().getSubtypes()).containsExactly(CardSubtype.FRACTAL);
            assertThat(fractal.getCard().getPower()).isZero();
            assertThat(fractal.getCard().getToughness()).isZero();
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(fractal.getEffectivePower()).isEqualTo(1);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each casting puts counters only on the token it creates")
    void subsequentCastingDoesNotAddCountersToEarlierToken() {
        harness.setHand(player1, List.of(new FractalSummoning(), new FractalSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.castAndResolveSorcery(player1, 0, 2);
        Permanent firstFractal = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(firstFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(firstFractal.getId()))
                .singleElement().satisfies(fractal -> {
                    assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
                    assertThat(fractal.getEffectivePower()).isEqualTo(3);
                    assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
                });
    }
}
