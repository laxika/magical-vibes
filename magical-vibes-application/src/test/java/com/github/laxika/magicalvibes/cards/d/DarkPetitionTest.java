package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkPetitionTest extends BaseCardTest {

    @Test
    @DisplayName("Searching puts the chosen card into hand")
    void searchPutsCardIntoHand() {
        setupAndCast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        int bearsIndex = indexOf("Grizzly Bears");
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(bearsIndex));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dark Petition");
    }

    @Test
    @DisplayName("Spell mastery adds three black mana with two instants/sorceries in the graveyard")
    void spellMasteryAddsThreeBlackMana() {
        harness.setGraveyard(player1, List.of(new Shock(), new DiabolicTutor()));
        setupAndCast();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("No mana is added with only one instant or sorcery in the graveyard")
    void noManaWithSingleInstantInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));
        setupAndCast();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Dark Petition itself does not count toward spell mastery")
    void ownCardDoesNotCountTowardSpellMastery() {
        harness.setGraveyard(player1, List.of(new Shock()));
        setupAndCast();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    private int indexOf(String name) {
        List<Card> cards = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Not offered: " + name);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new DarkPetition()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears()));
    }
}
