package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BywayCourier;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevilsPlayground.class, BywayCourier.class, FieryTemper.class})
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
        Permanent courier = harness.addToBattlefieldAndReturn(player2, new BywayCourier());

        killDevil(devil);
        harness.handlePermanentChosen(player1, courier.getId());
        harness.passBothPriorities();

        assertThat(courier.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Devil's death trigger can target its controller")
    void devilDeathCanDamageItsController() {
        Permanent devil = castDevilsPlaygroundAndGetDevil();
        harness.setLife(player1, 20);

        killDevil(devil);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Devil")).hasSize(3);
    }

    @Test
    @DisplayName("Killing another Devil with a death trigger produces a separate death trigger")
    void devilDeathCanKillAnotherDevil() {
        castDevilsPlayground();
        List<Permanent> devils = findPermanents(player1, "Devil");
        harness.setLife(player2, 20);

        killDevil(devils.get(0));
        harness.handlePermanentChosen(player1, devils.get(1).getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Devil")).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void castDevilsPlayground() {
        harness.setHand(player1, List.of(new DevilsPlayground()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);
    }

    private Permanent castDevilsPlaygroundAndGetDevil() {
        castDevilsPlayground();
        return findPermanents(player1, "Devil").getFirst();
    }

    private void killDevil(Permanent devil) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, devil.getId());
    }
}
