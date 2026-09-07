package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LonghornFirebeast.class})
class LonghornFirebeastTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.setHand(player1, List.of(new LonghornFirebeast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent declines — Firebeast stays and no damage is dealt")
    void decliningKeepsFirebeast() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Longhorn Firebeast");
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Longhorn Firebeast");
    }

    @Test
    @DisplayName("Opponent accepts — takes 5 damage and Firebeast is sacrificed")
    void acceptingDamagesAndSacrifices() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Longhorn Firebeast");
        harness.assertInGraveyard(player1, "Longhorn Firebeast");
    }
}
