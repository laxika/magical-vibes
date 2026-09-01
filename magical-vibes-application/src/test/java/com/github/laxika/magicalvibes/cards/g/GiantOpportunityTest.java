package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FeastingTrollKing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantOpportunity.class, FeastingTrollKing.class})
class GiantOpportunityTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing two Foods creates a 7/7 Giant")
    void sacrificesTwoFoodsForGiant() {
        castFoodProducer();
        List<Permanent> foods = foodPermanents();

        castGiantOpportunity();
        harness.handleMultiplePermanentsChosen(player1,
                foods.subList(0, 2).stream().map(Permanent::getId).toList());

        assertThat(countPermanents(player1, "Food")).isOne();
        Permanent giant = findPermanent(player1, "Giant");
        assertThat(giant.getCard().getPower()).isEqualTo(7);
        assertThat(giant.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Choosing not to sacrifice Foods creates three Food tokens")
    void declinesSacrificeForFoodTokens() {
        castFoodProducer();

        castGiantOpportunity();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(countPermanents(player1, "Food")).isEqualTo(6);
        harness.assertNotOnBattlefield(player1, "Giant");
    }

    @Test
    @DisplayName("Creates three Food tokens without a choice when fewer than two Foods are available")
    void createsFoodTokensWithoutEnoughFoods() {
        castGiantOpportunity();

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Giant");
    }

    private void castFoodProducer() {
        harness.setHand(player1, List.of(new FeastingTrollKing()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castGiantOpportunity() {
        harness.setHand(player1, List.of(new GiantOpportunity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Permanent> foodPermanents() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Food".equals(permanent.getCard().getName()))
                .toList();
    }
}
