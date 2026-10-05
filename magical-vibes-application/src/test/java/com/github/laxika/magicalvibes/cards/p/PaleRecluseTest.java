package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaleRecluse.class, Forest.class, Plains.class, Swamp.class, GrizzlyBears.class})
class PaleRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Forestcycling discards the card and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pale Recluse");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Forest from the search puts it into hand")
    void choosingForestPutsItIntoHand() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndOffersPlains() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pale Recluse");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains"))
                .hasSize(1);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Plains(),
                new Swamp(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Plainscycling puts the selected Plains into hand without drawing another card")
    void choosingPlainsPutsOnlyPlainsIntoHand() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Forestcycling discards as a cost before its search resolves")
    void discardsBeforeResolution() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Pale Recluse");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Forestcycling may fail to find even when a Forest is available")
    void mayFailToFind() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling resolves without drawing when no Forest is available")
    void noMatchingForest() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pale Recluse");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither cycling ability can be activated with only one mana")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new PaleRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> gs.activateHandAbility(gd, player1, 0, index, null))
                    .isInstanceOf(IllegalStateException.class);
            harness.assertInHand(player1, "Pale Recluse");
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.stack).isEmpty();
        }
    }
}
