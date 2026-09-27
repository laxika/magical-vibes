package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SacredFoundry;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Farseek.class, Plains.class, Island.class, Swamp.class, Mountain.class, Forest.class,
        SacredFoundry.class, BirdsOfParadise.class})
class FarseekTest extends BaseCardTest {

    @Test
    @DisplayName("Offers cards with Plains, Island, Swamp or Mountain types, but not Forest or nonlands")
    void offersNonForestLandTypes() {
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new Swamp(), new Mountain(),
                new SacredFoundry(), new Forest(), new BirdsOfParadise()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Island", "Swamp", "Mountain", "Sacred Foundry");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Chosen land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new Swamp(), new Mountain(),
                new SacredFoundry(), new Forest(), new BirdsOfParadise()));

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find and still finish when the library has no matching land type")
    void noMatchingLandTypeFinishesSearch() {
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(new Forest(), new BirdsOfParadise()));

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
