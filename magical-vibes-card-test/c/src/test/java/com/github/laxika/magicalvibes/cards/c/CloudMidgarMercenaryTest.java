package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.MageSlayer;
import com.github.laxika.magicalvibes.cards.s.Skullclamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudMidgarMercenary.class, LeoninScimitar.class, MageSlayer.class, GrizzlyBears.class, Skullclamp.class})
class CloudMidgarMercenaryTest extends BaseCardTest {

    @Test
    @DisplayName("Cloud searches for an Equipment and puts the chosen card into hand")
    void searchesForEquipment() {
        harness.setHand(player1, List.of(new CloudMidgarMercenary()));
        harness.setLibrary(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Leonin Scimitar");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cloud doubles a trigger from an Equipment attached to it")
    void doublesAttachedEquipmentTrigger() {
        harness.setLife(player2, 20);
        Permanent cloud = addCreatureReady(player1, new CloudMidgarMercenary());
        Permanent slayer = addCreatureReady(player2, new MageSlayer());
        slayer.setAttachedTo(cloud.getId());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() == slayer.getCard())
                .hasSize(2);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS,
                this::resolveAllTriggers);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cloud does not double a trigger from an Equipment attached elsewhere")
    void doesNotDoubleEquipmentAttachedElsewhere() {
        harness.setLife(player2, 20);
        Permanent cloud = addCreatureReady(player1, new CloudMidgarMercenary());
        Permanent scimitar = addCreatureReady(player1, new LeoninScimitar());
        scimitar.setAttachedTo(cloud.getId());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent slayer = addCreatureReady(player1, new MageSlayer());
        slayer.setAttachedTo(bears.getId());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS,
                () -> declareAttackers(player1, List.of(0, 2)));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() == slayer.getCard())
                .hasSize(1);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS,
                this::resolveAllTriggers);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cloud doubles attached Skullclamp's death trigger using the state before Cloud died")
    void doublesAttachedEquipmentDeathTrigger() {
        Permanent cloud = harness.addToBattlefieldAndReturn(player1, new CloudMidgarMercenary());
        harness.addToBattlefield(player1, new Skullclamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CloudMidgarMercenary(), new CloudMidgarMercenary(),
                new CloudMidgarMercenary(), new CloudMidgarMercenary()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, cloud.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cloud, Midgar Mercenary");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cloud's restricted search may fail to find an Equipment that is present")
    void canFailToFindEquipment() {
        harness.setLibrary(player1, List.of(new LeoninScimitar()));
        harness.enterBattlefieldAndReturn(player1, new CloudMidgarMercenary());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Cloud searches and shuffles an empty library without requesting a choice")
    void emptyLibraryDoesNotPrompt() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new CloudMidgarMercenary());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Cloud leaves non-Equipment cards in the library and still shuffles")
    void noEquipmentDoesNotPrompt() {
        Card nonEquipment = new CloudMidgarMercenary();
        harness.setLibrary(player1, List.of(nonEquipment));
        harness.enterBattlefieldAndReturn(player1, new CloudMidgarMercenary());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonEquipment);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

}
