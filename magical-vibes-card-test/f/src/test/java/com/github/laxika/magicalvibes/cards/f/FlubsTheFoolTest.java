package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlubsTheFool.class, Forest.class, GrizzlyBears.class})
class FlubsTheFoolTest extends BaseCardTest {

    @Test
    @DisplayName("Lets its controller play an additional land each turn")
    void grantsAdditionalLandPlay() {
        harness.addToBattlefield(player1, new FlubsTheFool());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws when a land is played with an empty hand")
    void drawsOnLandPlayWithEmptyHand() {
        harness.addToBattlefield(player1, new FlubsTheFool());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discards when a land is played with cards in hand")
    void discardsOnLandPlayWithCardsInHand() {
        harness.addToBattlefield(player1, new FlubsTheFool());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws or discards when its controller casts a spell")
    void triggersOnSpellCast() {
        harness.addToBattlefield(player1, new FlubsTheFool());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
