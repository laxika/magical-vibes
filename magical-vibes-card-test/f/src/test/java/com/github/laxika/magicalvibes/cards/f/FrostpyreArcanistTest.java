package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvalancheCaller;
import com.github.laxika.magicalvibes.cards.b.BeholdTheMultiverse;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.q.Quakebringer;
import com.github.laxika.magicalvibes.cards.r.Ravenform;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostpyreArcanist.class, AvalancheCaller.class, Mistwalker.class, Quakebringer.class,
        Ravenform.class, BeholdTheMultiverse.class})
class FrostpyreArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {4}{U} without a Giant or Wizard")
    void costsFullManaWithoutGiantOrWizard() {
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs {3}{U} while controlling a Giant")
    void costsReducedManaWithGiant() {
        harness.addToBattlefield(player1, new Quakebringer());
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs {3}{U} while controlling a Wizard")
    void costsReducedManaWithWizard() {
        harness.addToBattlefield(player1, new AvalancheCaller());
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("ETB search offers an instant or sorcery sharing a graveyard name")
    void etbSearchOffersMatchingInstantOrSorcery() {
        harness.setGraveyard(player1, List.of(new BeholdTheMultiverse()));
        setLibrary(new BeholdTheMultiverse(), new Ravenform(), new Mistwalker());
        castFrostpyreArcanist();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Behold the Multiverse");
    }

    @Test
    @DisplayName("ETB search puts the chosen matching card into hand")
    void etbSearchPutsChosenCardIntoHand() {
        harness.setGraveyard(player1, List.of(new BeholdTheMultiverse()));
        setLibrary(new BeholdTheMultiverse(), new Ravenform());
        castFrostpyreArcanist();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Behold the Multiverse");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Multiple Giants and Wizards reduce the cost only once")
    void multipleQualifyingPermanentsReduceCostOnlyOnce() {
        harness.addToBattlefield(player1, new Quakebringer());
        harness.addToBattlefield(player1, new AvalancheCaller());
        harness.addToBattlefield(player1, new FrostpyreArcanist());
        castFrostpyreArcanist();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Giant or Wizard does not reduce the cost")
    void opponentsPermanentsDoNotReduceCost() {
        harness.addToBattlefield(player2, new Quakebringer());
        harness.addToBattlefield(player2, new AvalancheCaller());
        castFrostpyreArcanist();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Changeling qualifies for the cost reduction")
    void changelingReducesCost() {
        harness.addToBattlefield(player1, new Mistwalker());
        castFrostpyreArcanist();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The search can find a sorcery but excludes matching creatures")
    void searchFindsSorceryAndExcludesMatchingCreature() {
        harness.setGraveyard(player1, List.of(new Ravenform(), new Mistwalker()));
        setLibrary(new Ravenform(), new Mistwalker(), new BeholdTheMultiverse());
        castFrostpyreArcanist();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Ravenform");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Ravenform");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mistwalker", "Behold the Multiverse");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Ravenform", "Mistwalker");
    }

    @Test
    @DisplayName("Cards in the opponent's graveyard do not qualify")
    void opponentsGraveyardDoesNotProvideMatchingNames() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Ravenform()));
        setLibrary(new Ravenform());
        castFrostpyreArcanist();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ravenform");
    }

    @Test
    @DisplayName("The controller may fail to find even when a matching card exists")
    void mayFailToFindMatchingCard() {
        harness.setGraveyard(player1, List.of(new Ravenform()));
        setLibrary(new Ravenform());
        castFrostpyreArcanist();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ravenform");
    }

    @Test
    @DisplayName("The search checks graveyard names when the trigger resolves")
    void searchUsesGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of(new Ravenform()));
        setLibrary(new Ravenform(), new BeholdTheMultiverse());
        castFrostpyreArcanist();
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new BeholdTheMultiverse()));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Behold the Multiverse");
    }

    @Test
    @DisplayName("An empty library ends the search without a choice")
    void emptyLibraryCompletesSearch() {
        harness.setGraveyard(player1, List.of(new Ravenform()));
        setLibrary();
        castFrostpyreArcanist();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castFrostpyreArcanist() {
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
