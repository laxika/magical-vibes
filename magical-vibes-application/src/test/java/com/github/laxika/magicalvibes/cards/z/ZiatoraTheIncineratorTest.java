package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZiatoraTheIncinerator.class, GrizzlyBears.class})
class ZiatoraTheIncineratorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature deals its power as damage and creates three Treasures")
    void sacrificeDealsPowerDamageAndCreatesTreasures() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Declining the sacrifice creates no Treasures and deals no damage")
    void decliningSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Having no other creature to sacrifice creates no Treasures")
    void noOtherCreatureDoesNothing() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void resolveEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(activePlayer.getId());
    }
}
