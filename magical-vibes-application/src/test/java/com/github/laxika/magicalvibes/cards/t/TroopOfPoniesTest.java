package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TroopOfPonies.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class TroopOfPoniesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Troop of Ponies sacrifices it and puts the ability on the stack")
    void activatingSacrificesSelf() {
        addCreatureReady(player1, new TroopOfPonies());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Troop of Ponies");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving presents basic lands for the battlefield and hand split")
    void resolvingPresentsBasicLands() {
        setupAndActivate();
        seedLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().followUp().basicLandToHand()).isNotNull();
    }

    @Test
    @DisplayName("Choosing two basic lands puts one tapped onto the battlefield and the other into hand")
    void choosesTwoBasicLands() {
        setupAndActivate();
        seedLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No basic lands in the library resolves without a search prompt")
    void noBasicLandsNoPrompt() {
        setupAndActivate();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Both selected lands are revealed and their destinations can be chosen")
    void revealsChosenLandsAndShuffles() {
        setupAndActivate();
        seedLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInHand(player1, "Plains");
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gameLogContains("reveals Plains")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An already tapped Troop of Ponies cannot pay the tap cost")
    void tappedCreatureCannotActivate() {
        addCreatureReady(player1, new TroopOfPonies()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Troop of Ponies");
        harness.assertNotInGraveyard(player1, "Troop of Ponies");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finding no lands is allowed even when basic lands are available")
    void mayFindZeroLands() {
        setupAndActivate();
        seedLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A single available basic land enters tapped and does not go to hand")
    void oneAvailableLandEntersTapped() {
        setupAndActivate();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Finding only one land is allowed when more basics remain")
    void mayDeclineSecondLand() {
        setupAndActivate();
        seedLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The ability resolves with an empty library")
    void emptyLibraryResolves() {
        setupAndActivate();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new TroopOfPonies());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Troop of Ponies");
        harness.assertNotInGraveyard(player1, "Troop of Ponies");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated with only one mana")
    void insufficientManaPreventsActivation() {
        addCreatureReady(player1, new TroopOfPonies());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Troop of Ponies");
        assertThat(findPermanent(player1, "Troop of Ponies").isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Troop of Ponies");
        assertThat(gd.stack).isEmpty();
    }

    private void setupAndActivate() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new TroopOfPonies());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
    }

    private void seedLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
