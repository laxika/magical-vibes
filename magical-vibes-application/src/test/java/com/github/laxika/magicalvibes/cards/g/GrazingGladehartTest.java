package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrazingGladehart.class, Forest.class})
class GrazingGladehartTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers 2 life and gains it when accepted")
    void gainsLifeWhenLandfallIsAccepted() {
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Landfall gains no life when declined")
    void doesNotGainLifeWhenLandfallIsDeclined() {
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Landfall does not trigger for an opponent's land")
    void doesNotTriggerForOpponentsLand() {
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Landfall triggers when a land enters without being played")
    void gainsLifeForLandEnteringWithoutBeingPlayed() {
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Gladehart offers an independent landfall choice")
    void multipleGladehartsOfferIndependentChoices() {
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void doesNotTriggerForNonlandEntering() {
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new GrazingGladehart());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }
}
