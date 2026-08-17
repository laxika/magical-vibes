package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DevilsPlaygroundTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four Devil creature tokens")
    void createsFourDevils() {
        castDevilsPlayground();

        assertThat(findPermanents(player1, "Devil")).hasSize(4);
    }

    @Test
    @DisplayName("A Devil that dies deals 1 damage to a target player")
    void devilDeathDealsDamageToPlayer() {
        Permanent devil = castDevilsPlaygroundAndGetDevil();
        harness.setLife(player2, 20);

        killDevil(devil);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A Devil that dies deals 1 damage to a target creature")
    void devilDeathDealsDamageToCreature() {
        Permanent devil = castDevilsPlaygroundAndGetDevil();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killDevil(devil);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    private void castDevilsPlayground() {
        harness.setHand(player1, List.of(new DevilsPlayground()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent castDevilsPlaygroundAndGetDevil() {
        castDevilsPlayground();
        return findPermanents(player1, "Devil").getFirst();
    }

    private void killDevil(Permanent devil) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();
    }
}
