package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BedheadBeastie.class, Mountain.class, GrizzlyBears.class})
class BedheadBeastieTest extends BaseCardTest {

    @Test
    @DisplayName("Mountaincycling discards the card and searches for a Mountain")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new BedheadBeastie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bedhead Beastie");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .isInstanceOf(Mountain.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
    }

    @Test
    void mountaincyclingPaysGenericManaAndDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new BedheadBeastie()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Bedhead Beastie");
        harness.assertNotInHand(player1, "Bedhead Beastie");
        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mountaincyclingCannotBeActivatedWithOnlyOneMana() {
        harness.setHand(player1, List.of(new BedheadBeastie()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bedhead Beastie");
        harness.assertNotInGraveyard(player1, "Bedhead Beastie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void mountaincyclingCanFailToFindAnAvailableMountain() {
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(new BedheadBeastie()));
        harness.setLibrary(player1, List.of(mountain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Bedhead Beastie");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mountaincyclingResolvesWhenNoMountainExists() {
        BedheadBeastie libraryCard = new BedheadBeastie();
        harness.setHand(player1, List.of(new BedheadBeastie()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Bedhead Beastie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new BedheadBeastie());
        addCreatureReady(player2, new BedheadBeastie());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new BedheadBeastie());
        Permanent first = addCreatureReady(player2, new BedheadBeastie());
        Permanent second = addCreatureReady(player2, new BedheadBeastie());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
