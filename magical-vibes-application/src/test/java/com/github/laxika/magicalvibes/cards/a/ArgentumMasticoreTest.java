package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerumCoreChimera;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArgentumMasticoreTest extends BaseCardTest {

    @Test
    void discardingACardDestroysAnOpponentNonlandPermanentWithinItsManaValue() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Argentum Masticore");
    }

    @Test
    void discardingACardDoesNotRequireADestructionTarget() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        harness.addToBattlefield(player2, new SerumCoreChimera());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Argentum Masticore");
        harness.assertOnBattlefield(player2, "Serum-Core Chimera");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningToDiscardSacrificesArgentumMasticore() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Argentum Masticore");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void resolveUpkeepTrigger() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
