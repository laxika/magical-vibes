package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SagesRowDenizen.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, ShiftingSky.class})
class SagesRowDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Another blue creature entering makes target player mill two cards")
    void blueCreatureEnteringMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A nonblue creature entering does not trigger milling")
    void nonblueCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can target themself for the mill")
    void canTargetController() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Denizen does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new SagesRowDenizen(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sage's Row Denizen");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's blue creature does not trigger Denizen")
    void opponentsBlueCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A second Denizen triggers only the Denizen already on the battlefield")
    void anotherDenizenTriggersExistingDenizenOnly() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new SagesRowDenizen(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Milling two cards from a one-card library mills only the remaining card")
    void shortLibraryMillsOnlyAvailableCard() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        Forest remainingCard = new Forest();
        harness.setLibrary(player2, List.of(remainingCard));

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("The trigger mills the top two cards rather than arbitrary library cards")
    void millsTopTwoCards() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        Forest topCard = new Forest();
        GrizzlyBears secondCard = new GrizzlyBears();
        FugitiveWizard bottomCard = new FugitiveWizard();
        harness.setLibrary(player2, List.of(topCard, secondCard, bottomCard));

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottomCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(topCard, secondCard);
    }

    @Test
    @DisplayName("A creature made blue by Shifting Sky triggers Denizen as it enters")
    void creatureEnteringAsBlueDueToContinuousEffectTriggers() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.addToBattlefieldAndReturn(player1, new ShiftingSky()).setChosenColor(CardColor.BLUE);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A blue card entering as green because of Shifting Sky does not trigger Denizen")
    void creatureEnteringAsNonblueDueToContinuousEffectDoesNotTrigger() {
        harness.addToBattlefield(player1, new SagesRowDenizen());
        harness.addToBattlefieldAndReturn(player1, new ShiftingSky()).setChosenColor(CardColor.GREEN);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
