package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.i.IreOfKaminari;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyokiSanitysEclipse.class, IreOfKaminari.class, KamiOfFalseHope.class, GoblinCohort.class})
class KyokiSanitysEclipseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell makes the targeted opponent exile a card of their choice")
    void arcaneCastExilesFromOpponentHand() {
        harness.addToBattlefield(player1, new KyokiSanitysEclipse());
        harness.setHand(player1, List.of(new IreOfKaminari()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new GoblinCohort(), new KamiOfFalseHope()));

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Cohort"));
        harness.assertNotInGraveyard(player2, "Goblin Cohort");
    }

    @Test
    @DisplayName("Casting a Spirit spell also triggers the exile")
    void spiritCastExilesFromOpponentHand() {
        harness.addToBattlefield(player1, new KyokiSanitysEclipse());
        harness.setHand(player2, List.of(new GoblinCohort()));

        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Cohort"));
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KyokiSanitysEclipse());
        harness.setHand(player2, List.of(new GoblinCohort()));

        harness.castFromHand(player1, new GoblinCohort(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the targeted opponent has no cards in hand")
    void noCardsInTargetOpponentsHandDoesNotCreateChoice() {
        harness.addToBattlefield(player1, new KyokiSanitysEclipse());
        harness.setHand(player2, List.of());

        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent casting an Arcane spell does not trigger Kyoki")
    void opponentsArcaneSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KyokiSanitysEclipse());
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.setHand(player2, List.of(new IreOfKaminari()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goblin Cohort");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A queued trigger still exiles a card after Kyoki leaves the battlefield")
    void triggerResolvesAfterKyokiLeaves() {
        var kyoki = harness.addToBattlefieldAndReturn(player1, new KyokiSanitysEclipse());
        harness.setHand(player2, List.of(new GoblinCohort()));
        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.handlePermanentChosen(player1, player2.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kyoki));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Cohort"));
        harness.assertInGraveyard(player1, "Kyoki, Sanity's Eclipse");
    }
}
