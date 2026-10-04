package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalimarExcavator.class, SeaGateLoremaster.class, GrizzlyBears.class, Conspiracy.class, IntoTheRoil.class})
class HalimarExcavatorTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry mills cards equal to the number of Allies you control")
    void ownAllyEntryMillsForAllyCount() {
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.castFromHand(player1, new HalimarExcavator(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Another Ally entering triggers Halimar Excavator")
    void anotherAllyEntryTriggers() {
        harness.addToBattlefield(player1, new HalimarExcavator());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.castFromHand(player1, new SeaGateLoremaster(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HalimarExcavator());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A lone Excavator can target its controller and ignores opposing Allies in the count")
    void canMillItsController() {
        harness.addToBattlefield(player2, new HalimarExcavator());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.castFromHand(player1, new HalimarExcavator(), "{1}{U}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Two Excavators each trigger and count both Allies")
    void multipleExcavatorsTriggerIndependently() {
        harness.addToBattlefield(player1, new HalimarExcavator());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.castFromHand(player1, new HalimarExcavator(), "{1}{U}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the source before resolution reduces the Ally count but does not remove its trigger")
    void countsAlliesAtResolutionAfterSourceLeaves() {
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.castFromHand(player1, new HalimarExcavator(), "{1}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Halimar Excavator"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Halimar Excavator");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A short library is milled completely without causing an immediate loss")
    void millsOnlyAvailableCards() {
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.setLibrary(player2, List.of(new HalimarExcavator()));
        harness.castFromHand(player1, new HalimarExcavator(), "{1}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Its own entry still requires a target when Conspiracy removes the Ally subtype")
    void ownEntryTriggersWithoutAllySubtype() {
        var conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.castFromHand(player1, new HalimarExcavator(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        );
    }
}
