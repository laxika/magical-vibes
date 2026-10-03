package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurnishedHart.class, Plains.class, Forest.class, Island.class, BronzeSable.class})
class BurnishedHartTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Burnished Hart sacrifices it and puts the ability on the stack")
    void activatingSacrificesSelf() {
        addHartAndMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Burnished Hart");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving presents up to two basic lands for the battlefield-tapped search")
    void resolvingPresentsBasicLands() {
        addHartAndMana();
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Choosing two basic lands puts both onto the battlefield tapped")
    void choosesTwoBasicLands() {
        addHartAndMana();
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .anyMatch(card -> card instanceof BronzeSable)
                .anyMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No basic lands in the library resolves without a search prompt")
    void noBasicLandsNoPrompt() {
        addHartAndMana();
        harness.setLibrary(player1, List.of(new BronzeSable()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void canChooseZeroLandsDespiteAvailableBasics() {
        addHartAndMana();
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canStopAfterOneLand() {
        addHartAndMana();
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyOneAvailableBasicEntersTappedAndFinishesSearch() {
        addHartAndMana();
        harness.setLibrary(player1, List.of(new Forest(), new BronzeSable()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof BronzeSable);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chosenLandsWaitUntilSelectionIsCompleteToEnterTogether() {
        addHartAndMana();
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addHartAndMana() {
        harness.addToBattlefield(player1, new BurnishedHart());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void seedLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new BronzeSable()));
    }
}
