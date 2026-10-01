package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.cards.s.SapseepForest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FarhavenElf.class, Forest.class, Island.class, Plains.class, SapseepForest.class})
class FarhavenElfTest extends BaseCardTest {

    @Test
    @DisplayName("Farhaven Elf ETB creates a may prompt")
    void etbCreatesMayPrompt() {
        harness.castFromHand(player1, new FarhavenElf(), "{2}{G}");

        harness.passBothPriorities(); // resolve creature spell → ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting offers only basic lands, destination battlefield tapped")
    void acceptingOffersBasicLandsToBattlefieldTapped() {
        harness.castFromHand(player1, new FarhavenElf(), "{2}{G}");
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Accepting with no basic land leaves the library unchanged")
    void acceptingWithNoBasicLandLeavesLibraryUnchanged() {
        harness.castFromHand(player1, new FarhavenElf(), "{2}{G}");
        harness.setLibrary(player1, List.of(new SapseepForest(), new FarhavenElf()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chosen basic land enters battlefield tapped")
    void chosenBasicLandEntersTapped() {
        harness.castFromHand(player1, new FarhavenElf(), "{2}{G}");
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningSkipsSearch() {
        harness.castFromHand(player1, new FarhavenElf(), "{2}{G}");
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Island(), new SapseepForest(), new FarhavenElf()));
    }
}
