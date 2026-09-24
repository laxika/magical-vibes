package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoseFocus.class, YouthfulKnight.class})
class LoseFocusTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay {2}")
    void countersWhenControllerCannotPay() {
        YouthfulKnight knight = castKnight();
        castLoseFocus(knight, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("The target spell resolves when its controller pays {2}")
    void resolvesWhenControllerPays() {
        YouthfulKnight knight = castKnight();
        harness.addMana(player1, ManaColor.BLUE, 2);
        castLoseFocus(knight, List.of());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("Replicate creates one copy for each additional {U} paid")
    void replicateCreatesCopies() {
        YouthfulKnight knight = castKnight();
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, knight.getId(), List.of("{U}", "{U}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Youthful Knight");
    }

    private YouthfulKnight castKnight() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");
        return knight;
    }

    private void castLoseFocus(YouthfulKnight target, List<String> replicatePayments) {
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 2 + replicatePayments.size());
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, target.getId(), replicatePayments);
    }
}
