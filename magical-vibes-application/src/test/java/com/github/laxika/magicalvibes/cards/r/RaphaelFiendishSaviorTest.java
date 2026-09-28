package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VexingDevil;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphaelFiendishSavior.class, VexingDevil.class, GrizzlyBears.class})
class RaphaelFiendishSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other matching creatures you control and gives them lifelink")
    void boostsMatchingCreaturesYouControl() {
        Permanent ownDevil = harness.addToBattlefieldAndReturn(player1, new VexingDevil());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDevil = harness.addToBattlefieldAndReturn(player2, new VexingDevil());
        int devilPower = gqs.getEffectivePower(gd, ownDevil);
        int devilToughness = gqs.getEffectiveToughness(gd, ownDevil);
        int bearPower = gqs.getEffectivePower(gd, ownBear);
        int bearToughness = gqs.getEffectiveToughness(gd, ownBear);
        int opponentDevilPower = gqs.getEffectivePower(gd, opponentDevil);
        int opponentDevilToughness = gqs.getEffectiveToughness(gd, opponentDevil);

        harness.addToBattlefield(player1, new RaphaelFiendishSavior());

        assertThat(gqs.getEffectivePower(gd, ownDevil)).isEqualTo(devilPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownDevil)).isEqualTo(devilToughness + 1);
        assertThat(gqs.hasKeyword(gd, ownDevil, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(bearToughness);
        assertThat(gqs.getEffectivePower(gd, opponentDevil)).isEqualTo(opponentDevilPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentDevil)).isEqualTo(opponentDevilToughness);
        assertThat(gqs.hasKeyword(gd, opponentDevil, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creates a Devil at each end step after a creature card enters your graveyard")
    void createsDevilAfterCreatureCardEntersOwnGraveyard() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Devil")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Devil when only an opponent's creature enters their graveyard")
    void doesNotCreateDevilForOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    @DisplayName("A Devil token deals 1 damage to a chosen target when it dies")
    void devilTokenDealsDamageWhenItDies() {
        harness.addToBattlefield(player1, new RaphaelFiendishSavior());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        advanceToEndStep(player1);

        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, devil));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
