package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommunalBrewing.class, GrizzlyBears.class})
class CommunalBrewingTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted opponents draw and add ingredient counters for cards drawn")
    void targetedOpponentsDrawAndAddIngredients() {
        harness.setHand(player2, List.of());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(drawnCard));
        castCommunalBrewing(List.of(player2.getId()));

        Permanent brewing = communalBrewing();
        assertThat(brewing.getCounterCount(CounterType.INGREDIENT)).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A creature cast afterward enters with one counter per ingredient")
    void creatureEntersWithIngredientCounters() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        castCommunalBrewing(List.of(player2.getId()));

        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Communal Brewing can enter without choosing an opponent")
    void canChooseNoOpponents() {
        harness.setHand(player2, List.of());
        castCommunalBrewing(List.of());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(communalBrewing().getCounterCount(CounterType.INGREDIENT)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Communal Brewing cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new CommunalBrewing()));
        addManaForCommunalBrewing();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castCommunalBrewing(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new CommunalBrewing()));
        addManaForCommunalBrewing();
        harness.castEnchantment(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addManaForCommunalBrewing() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent communalBrewing() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CommunalBrewing)
                .findFirst()
                .orElseThrow();
    }
}
