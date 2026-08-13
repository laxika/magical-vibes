package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DriftingDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} at upkeep keeps Drifting Djinn on the battlefield")
    void payAtUpkeepKeepsIt() {
        harness.addToBattlefield(player1, new DriftingDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drifting Djinn");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices Drifting Djinn")
    void declineAtUpkeepSacrificesIt() {
        harness.addToBattlefield(player1, new DriftingDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Drifting Djinn");
    }

    @Test
    @DisplayName("Cycling discards Drifting Djinn and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DriftingDjinn()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Drifting Djinn");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
