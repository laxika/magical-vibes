package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
