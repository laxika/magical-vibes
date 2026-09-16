package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HickoryWoodlot;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilvergladePathfinder.class, Plains.class, Forest.class, Island.class, HickoryWoodlot.class})
class SilvergladePathfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability discards a card and taps Silverglade Pathfinder")
    void activatingDiscardsCardAndTapsSource() {
        Permanent pathfinder = addReadyPathfinder();
        harness.setHand(player1, List.of(new SilvergladePathfinder()));
        addMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Silverglade Pathfinder");
        assertThat(pathfinder.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability offers only basic lands to enter tapped")
    void resolvingOffersOnlyBasicLandsToBattlefieldTapped() {
        activateSearch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Choosing a basic land puts it onto the battlefield tapped")
    void chosenBasicLandEntersTapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no basic land in the library, the ability resolves without a search prompt")
    void noBasicLandLeavesBattlefieldUnchanged() {
        addReadyPathfinder();
        harness.setHand(player1, List.of(new SilvergladePathfinder()));
        harness.setLibrary(player1, List.of(new HickoryWoodlot(), new SilvergladePathfinder()));
        addMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Cannot activate the ability without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyPathfinder();
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPathfinder() {
        return addCreatureReady(player1, new SilvergladePathfinder());
    }

    private void activateSearch() {
        addReadyPathfinder();
        harness.setHand(player1, List.of(new SilvergladePathfinder()));
        addMana();

        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Island(), new HickoryWoodlot(), new SilvergladePathfinder()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
