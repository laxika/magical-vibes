package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilFrillback.class, FountainOfYouth.class, GrizzlyBears.class})
class TranquilFrillbackTest extends BaseCardTest {

    @Test
    void paysUpToThreeTimesAndChoosesThatManyModes() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice paymentChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(paymentChoice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 3);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");
        harness.handleListChoice(player1, "Exile target player's graveyard");
        harness.handleListChoice(player1, "You gain 4 life");

        PendingInteraction.PermanentChoice artifactChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(artifactChoice.validIds()).containsExactlyInAnyOrder(artifact.getId(), player1.getId());
        harness.handlePermanentChosen(player1, artifact.getId());

        PendingInteraction.PermanentChoice playerChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerChoice.validIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void decliningPaymentDoesNothing() {
        harness.setHand(player1, List.of(new TranquilFrillback()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
