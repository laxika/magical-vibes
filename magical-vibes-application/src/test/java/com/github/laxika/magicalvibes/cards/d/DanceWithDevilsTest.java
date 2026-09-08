package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DanceWithDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 red Devil creature tokens")
    void createsTwoDevils() {
        castDanceWithDevils();

        List<Permanent> devils = findPermanents(player1, "Devil");
        assertThat(devils).hasSize(2);
        for (Permanent devil : devils) {
            assertThat(devil.getCard().getPower()).isEqualTo(1);
            assertThat(devil.getCard().getToughness()).isEqualTo(1);
            assertThat(devil.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(devil.getCard().getSubtypes()).containsExactly(CardSubtype.DEVIL);
            assertThat(devil.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("A Devil that dies deals 1 damage to a target player")
    void devilDeathDealsDamageToPlayer() {
        Permanent devil = castDanceWithDevilsAndGetDevil();
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
        Permanent devil = castDanceWithDevilsAndGetDevil();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killDevil(devil);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    private void castDanceWithDevils() {
        harness.setHand(player1, List.of(new DanceWithDevils()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent castDanceWithDevilsAndGetDevil() {
        castDanceWithDevils();
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
