package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TestOfTalents.class, Forest.class, GrizzlyBears.class, Opt.class, Plains.class})
class TestOfTalentsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant, offers any number of same-name cards, and draws for selected hand copies")
    void countersAndDrawsForSelectedHandCopies() {
        Opt castCopy = new Opt();
        Opt handCopy = new Opt();
        Opt unselectedHandCopy = new Opt();
        Plains drawnCard = new Plains();
        Forest remainingLibraryCard = new Forest();

        harness.setHand(player1, List.of(castCopy, handCopy, unselectedHandCopy));
        harness.setLibrary(player1, List.of(drawnCard, remainingLibraryCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new TestOfTalents()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(handCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(handCopy).doesNotContain(unselectedHandCopy);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(castCopy);
        assertThat(gd.playerHands.get(player1.getId())).contains(unselectedHandCopy).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card == drawnCard || card == remainingLibraryCard)
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new TestOfTalents()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
