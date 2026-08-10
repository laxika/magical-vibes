package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThalakosScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card puts Thalakos Scout's return ability on the stack")
    void discardCostPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new ThalakosScout());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discarding a card returns Thalakos Scout to its owner's hand")
    void discardCostReturnsThalakosScoutToHand() {
        harness.addToBattlefield(player1, new ThalakosScout());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Thalakos Scout");
        harness.assertNotOnBattlefield(player1, "Thalakos Scout");
    }

    @Test
    @DisplayName("Thalakos Scout cannot activate without a card in hand")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new ThalakosScout());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
