package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GontiLordOfLuxury.class, GrizzlyBears.class, LlanowarElves.class,
        Shock.class, LightningBolt.class, Forest.class})
class GontiLordOfLuxuryTest extends BaseCardTest {

    private void castGonti(List<Card> opponentLibrary) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, opponentLibrary);
        harness.setHand(player1, List.of(new GontiLordOfLuxury()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles one of the target opponent's top four cards face down")
    void etbExilesOneTopCardAndBottomsTheRest() {
        List<Card> library = List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(),
                new LightningBolt(), new GrizzlyBears());
        castGonti(library);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(4);

        Card chosen = search.params().cards().get(2);
        harness.handleCardChosen(player1, 2);

        UUID gontiId = harness.getPermanentId(player1, "Gonti, Lord of Luxury");
        assertThat(gd.getCardsExiledByPermanent(gontiId)).containsExactly(chosen);
        assertThat(gd.exiledCards).filteredOn(e -> e.card().getId().equals(chosen.getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4)
                .containsExactlyInAnyOrder(library.get(0), library.get(1), library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May cast the selected card with any type of mana after Gonti leaves")
    void mayCastSelectedCardAfterGontiLeaves() {
        List<Card> library = List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(),
                new LightningBolt());
        castGonti(library);

        Card chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().get(0);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID gontiId = harness.getPermanentId(player1, "Gonti, Lord of Luxury");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, gontiId);
        harness.assertNotOnBattlefield(player1, "Gonti, Lord of Luxury");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void etbCannotTargetItsController() {
        harness.setHand(player1, List.of(new GontiLordOfLuxury()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A short library still exiles one card and bottoms all remaining cards")
    void handlesFewerThanFourCards() {
        Card chosen = new GrizzlyBears();
        Card remaining = new LlanowarElves();
        castGonti(List.of(chosen, remaining));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(chosen, remaining);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == chosen && e.faceDown()
                && e.ownerId().equals(player2.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes without requesting a card choice")
    void handlesEmptyLibrary() {
        castGonti(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gonti, Lord of Luxury");
    }

    @Test
    @DisplayName("The exiled creature must obey normal casting timing")
    void cannotCastCreatureDuringOpponentsTurn() {
        Card chosen = new GrizzlyBears();
        castGonti(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The card's owner cannot use Gonti's casting permission")
    void opponentCannotCastExiledCard() {
        Card chosen = new GrizzlyBears();
        castGonti(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == chosen);
    }

    @Test
    @DisplayName("Gonti permits casting spells but never playing an exiled land")
    void cannotPlayExiledLand() {
        Card chosen = new Forest();
        castGonti(List.of(chosen));
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == chosen && e.faceDown());
    }

    @Test
    @DisplayName("Unexamined cards stay above the cards put on the bottom")
    void preservesUnexaminedLibraryOrder() {
        Card fifth = new GrizzlyBears();
        Card sixth = new LlanowarElves();
        castGonti(List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(),
                new LightningBolt(), fifth, sixth));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .containsExactly(fifth, sixth);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Casting still requires paying the exiled card's full mana cost")
    void failedPaymentKeepsCastingPermission() {
        Card chosen = new GrizzlyBears();
        castGonti(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == chosen);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> e.card() == chosen);
    }

    @Test
    @DisplayName("An exiled instant can be cast on the opponent's turn using colorless mana")
    void castsInstantWithColorlessManaOnOpponentsTurn() {
        Card chosen = new Shock();
        castGonti(List.of(chosen));
        harness.handleCardChosen(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, chosen.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chosen);
        assertThat(gd.exiledCards).noneMatch(e -> e.card() == chosen);
    }
}
