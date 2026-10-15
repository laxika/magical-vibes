package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerumCoreChimera;
import com.github.laxika.magicalvibes.cards.t.ThrunBreakerOfSilence;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArgentumMasticore.class, GrizzlyBears.class, SerumCoreChimera.class,
        CopperLonglegs.class, Plains.class, ThrunBreakerOfSilence.class})
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

    @Test
    void emptyHandSacrificesTheSourceWithoutCreatingADestructionTrigger() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        harness.addToBattlefield(player2, new CopperLonglegs());
        harness.setHand(player1, List.of());

        resolveUpkeepTrigger();

        harness.assertInGraveyard(player1, "Argentum Masticore");
        harness.assertOnBattlefield(player2, "Copper Longlegs");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void destructionTargetsExcludeOwnPermanentsLandsAndHigherManaValues() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        harness.addToBattlefield(player1, new CopperLonglegs());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new SerumCoreChimera());
        harness.setHand(player1, List.of(new CopperLonglegs()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Copper Longlegs");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Copper Longlegs");
        harness.assertOnBattlefield(player1, "Copper Longlegs");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertOnBattlefield(player2, "Serum-Core Chimera");
    }

    @Test
    void discardingALandKeepsTheSourceWithoutTargetingANonzeroManaValuePermanent() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        harness.addToBattlefield(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new Plains()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Plains");
        harness.assertOnBattlefield(player1, "Argentum Masticore");
        harness.assertOnBattlefield(player2, "Copper Longlegs");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void upkeepAbilityStillAllowsDiscardAndDestructionAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArgentumMasticore());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new CopperLonglegs()));

        advanceToUpkeep(player1);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, source);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Argentum Masticore");
        harness.assertInGraveyard(player1, "Copper Longlegs");
        harness.assertInGraveyard(player2, "Copper Longlegs");
    }

    @Test
    void destructionCannotTargetThrunFromANongreenSource() {
        harness.addToBattlefield(player1, new ArgentumMasticore());
        harness.addToBattlefield(player2, new ThrunBreakerOfSilence());
        harness.setHand(player1, List.of(new ArgentumMasticore()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Argentum Masticore");
        harness.assertOnBattlefield(player2, "Thrun, Breaker of Silence");
        harness.assertInGraveyard(player1, "Argentum Masticore");
    }

    private void resolveUpkeepTrigger() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
