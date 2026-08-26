package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gigadrowse.class, GrizzlyBears.class})
class GigadrowseTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target permanent")
    void tapsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGigadrowse(target, List.of());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGigadrowse(target, List.of("{U}", "{U}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Replicate cannot target a player")
    void replicateCannotTargetPlayer() {
        harness.setHand(player1, List.of(new Gigadrowse()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent");
    }

    private void castGigadrowse(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Gigadrowse()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), replicatePayments);
    }
}
