package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SweepTheSkies.class)
class SweepTheSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Thopter for each distinct color spent")
    void createsOneThopterForEachDistinctColorSpent() {
        harness.setHand(player1, List.of(new SweepTheSkies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 2);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Thopter");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Repeated and colorless mana do not increase the token count")
    void countsEachColorOnce() {
        harness.setHand(player1, List.of(new SweepTheSkies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("X zero still creates one colorless flying Thopter artifact creature")
    void zeroXCreatesOneThopterWithAllCharacteristics() {
        harness.setHand(player1, List.of(new SweepTheSkies()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getName()).isEqualTo("Thopter");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sweep the Skies");
    }

    @Test
    @DisplayName("Spending all five colors creates five tokens independently of X")
    void allFiveColorsCreateFiveTokens() {
        harness.setHand(player1, List.of(new SweepTheSkies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5)
                .allSatisfy(token -> assertThat(token.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Unspent colors in the mana pool do not count toward Converge")
    void unusedManaColorsDoNotCount() {
        harness.setHand(player1, List.of(new SweepTheSkies()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }
}
