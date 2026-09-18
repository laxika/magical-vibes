package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TemptWithVengeance.class)
class TemptWithVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X Elementals for the controller and rewards an accepting opponent")
    void acceptingOpponentCreatesTokensForBothPlayersAndRewardsController() {
        castTemptWithVengeance(2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(elementals(player1)).hasSize(4);
        assertThat(elementals(player2)).hasSize(2);
        assertThat(elementals(player1)).allSatisfy(this::assertHastyElemental);
        assertThat(elementals(player2)).allSatisfy(this::assertHastyElemental);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant another batch")
    void decliningOpponentDoesNotCreateAdditionalTokens() {
        castTemptWithVengeance(2);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(elementals(player1)).hasSize(2);
        assertThat(elementals(player2)).isEmpty();
    }

    private void castTemptWithVengeance(int xValue) {
        harness.setHand(player1, List.of(new TemptWithVengeance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private List<Permanent> elementals(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanents(player, "Elemental").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void assertHastyElemental(Permanent elemental) {
        assertThat(elemental.getEffectivePower()).isEqualTo(1);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
    }
}
