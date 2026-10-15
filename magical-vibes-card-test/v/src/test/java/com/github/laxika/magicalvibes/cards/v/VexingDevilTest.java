package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexingDevil.class})
class VexingDevilTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new VexingDevil(), "{R}");
        harness.passBothPriorities(); // creature resolves → ETB trigger stacks
        harness.passBothPriorities(); // ETB resolves → opponent is offered the choice
    }

    @Test
    @DisplayName("Opponent declines — Devil stays, no damage")
    void decliningKeepsDevil() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Vexing Devil");
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Vexing Devil");
    }

    @Test
    @DisplayName("Opponent accepts — takes 4 damage and Devil is sacrificed")
    void acceptingDamagesAndSacrifices() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void acceptingStillSacrificesWhenAllDamageIsPrevented() {
        gd.playerDamagePreventionShields.put(player2.getId(), 4);
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void acceptingStillSacrificesWhenSomeDamageIsPrevented() {
        gd.playerDamagePreventionShields.put(player2.getId(), 2);
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void acceptingForSecondDevilDoesNotSacrificeFirstDevil() {
        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player2, false);
        var firstDevilId = harness.getPermanentId(player1, "Vexing Devil");

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getId())
                .containsExactly(firstDevilId);
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void triggerStillDealsDamageAfterDevilLeavesBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VexingDevil(), "{R}");
        harness.passBothPriorities();
        var devil = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, devil));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        harness.assertInHand(player1, "Vexing Devil");
        harness.assertNotInGraveyard(player1, "Vexing Devil");
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
    }
}
