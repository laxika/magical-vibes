package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrazcaPuzzleDoor.class, Forest.class, GrizzlyBears.class})
class OrazcaPuzzleDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Orazca Puzzle-Door puts one top card into hand and the other into the graveyard")
    void sacrificesAndSelectsTopCards() {
        Forest chosenCard = new Forest();
        GrizzlyBears graveyardCard = new GrizzlyBears();
        OrazcaPuzzleDoor door = new OrazcaPuzzleDoor();
        harness.setLibrary(player1, List.of(chosenCard, graveyardCard));
        harness.addToBattlefield(player1, door);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Orazca Puzzle-Door");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(door);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With one card in the library, Orazca Puzzle-Door puts it into hand without a choice")
    void oneCardLibrary() {
        Forest card = new Forest();
        OrazcaPuzzleDoor door = new OrazcaPuzzleDoor();
        harness.setLibrary(player1, List.of(card));
        harness.addToBattlefield(player1, door);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(door);
    }

    @Test
    @DisplayName("Orazca Puzzle-Door cannot be activated while tapped")
    void cannotActivateWhileTapped() {
        Permanent door = harness.addToBattlefieldAndReturn(player1, new OrazcaPuzzleDoor());
        door.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(door);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(door.getCard());
    }
}
