package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WernogRidersChaplain.class)
class WernogRidersChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent who investigates creates a Clue and increases the controller's Clues")
    void opponentInvestigates() {
        castWernog();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player2, "Clue")).isEqualTo(1);
        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent who declines loses 1 life and the controller investigates once")
    void opponentDeclines() {
        castWernog();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player2, "Clue")).isZero();
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability uses the same opponent choice")
    void leavesTheBattlefieldTriggersAbility() {
        Permanent wernog = harness.addToBattlefieldAndReturn(player1, new WernogRidersChaplain());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, wernog));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    private void castWernog() {
        harness.setHand(player1, List.of(new WernogRidersChaplain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
