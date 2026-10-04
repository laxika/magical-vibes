package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorillaTactics.class, Distress.class})
class GorillaTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 green Gorilla token when cast")
    void createsGorillaWhenCast() {
        harness.setHand(player1, List.of(new GorillaTactics()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertGorillas(player1, 1);
        harness.assertInGraveyard(player1, "Gorilla Tactics");
    }

    @Test
    @DisplayName("Creates two Gorilla tokens when discarded by an opponent")
    void createsTwoGorillasWhenDiscardedByOpponent() {
        harness.setHand(player2, List.of(new GorillaTactics()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertGorillas(player2, 2);
        harness.assertInGraveyard(player2, "Gorilla Tactics");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not create tokens when discarded by its owner's spell")
    void doesNotTriggerWhenDiscardedByOwnSpell() {
        harness.setHand(player1, List.of(new Distress(), new GorillaTactics()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gorilla Tactics");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void assertGorillas(Player player, int count) {
        List<Permanent> gorillas = gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Gorilla".equals(permanent.getCard().getName()))
                .toList();

        assertThat(gorillas).hasSize(count);
        assertThat(gorillas).allSatisfy(gorilla -> {
            assertThat(gorilla.getEffectivePower()).isEqualTo(2);
            assertThat(gorilla.getEffectiveToughness()).isEqualTo(2);
            assertThat(gorilla.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(gorilla.getCard().getSubtypes())
                    .extracting(CardSubtype::getDisplayName)
                    .containsExactly("Gorilla");
        });
    }
}
