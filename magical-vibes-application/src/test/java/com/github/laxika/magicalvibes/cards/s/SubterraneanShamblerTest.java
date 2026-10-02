package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubterraneanShambler.class, AshcoatBear.class, FledglingMawcor.class})
class SubterraneanShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 1 damage to nonflying creatures only")
    void etbDamagesNonflyingCreaturesOnly() {
        Permanent nonflying = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent flying = harness.addToBattlefieldAndReturn(player2, new FledglingMawcor());

        Permanent shambler = castShambler();

        assertThat(shambler.getMarkedDamage()).isEqualTo(1);
        assertThat(nonflying.getMarkedDamage()).isEqualTo(1);
        assertThat(flying.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Leaves-the-battlefield trigger deals 1 damage to nonflying creatures only")
    void leavesTheBattlefieldDamagesNonflyingCreaturesOnly() {
        Permanent nonflying = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent flying = harness.addToBattlefieldAndReturn(player2, new FledglingMawcor());
        Permanent shambler = castShambler();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shambler));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nonflying.getMarkedDamage()).isEqualTo(2);
        assertThat(flying.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining echo sacrifices Subterranean Shambler at its next upkeep")
    void decliningEchoSacrificesAtNextUpkeep() {
        castShambler();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Subterranean Shambler");
        harness.assertInGraveyard(player1, "Subterranean Shambler");
    }

    @Test
    @DisplayName("Paying echo keeps Subterranean Shambler and echo does not trigger again")
    void payingEchoKeepsShamblerAndIsOneShot() {
        castShambler();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Subterranean Shambler");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Subterranean Shambler");
    }

    @Test
    @DisplayName("Echo waits for Subterranean Shambler's controller's upkeep")
    void echoWaitsForControllerUpkeep() {
        castShambler();

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Subterranean Shambler");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent castShambler() {
        harness.castFromHand(player1, new SubterraneanShambler(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Subterranean Shambler");
    }
}
