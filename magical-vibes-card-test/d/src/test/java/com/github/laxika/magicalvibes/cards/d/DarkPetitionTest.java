package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.y.YevasForcemage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkPetition.class, FieryImpulse.class, YevasForcemage.class, Plains.class, Swamp.class})
class DarkPetitionTest extends BaseCardTest {

    @Test
    @DisplayName("Searching puts the chosen card into hand")
    void searchPutsCardIntoHand() {
        setupAndCast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOf("Yeva's Forcemage"));

        harness.assertInHand(player1, "Yeva's Forcemage");
        harness.assertInGraveyard(player1, "Dark Petition");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Spell mastery adds three black mana with two instants/sorceries in the graveyard")
    void spellMasteryAddsThreeBlackMana() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new DarkPetition()));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("No mana is added with only one instant or sorcery in the graveyard")
    void noManaWithSingleInstantInGraveyard() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new YevasForcemage()));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Dark Petition itself does not count toward spell mastery")
    void ownCardDoesNotCountTowardSpellMastery() {
        harness.setGraveyard(player1, List.of(new FieryImpulse()));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Spell mastery works with two instants")
    void spellMasteryWithTwoInstants() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new FieryImpulse()));
        setupAndCast();

        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Spell mastery works with two sorceries")
    void spellMasteryWithTwoSorceries() {
        harness.setGraveyard(player1, List.of(new DarkPetition(), new DarkPetition()));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's graveyard does not contribute to spell mastery")
    void opponentsGraveyardDoesNotCount() {
        harness.setGraveyard(player1, List.of(new FieryImpulse()));
        harness.setGraveyard(player2, List.of(new FieryImpulse(), new DarkPetition()));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("An empty library does not prevent spell mastery from adding mana")
    void emptyLibraryStillAddsMana() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new DarkPetition()));
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Dark Petition");
    }

    @Test
    @DisplayName("Searching can put an instant into hand without counting it for spell mastery")
    void searchedInstantDoesNotCountTowardSpellMastery() {
        harness.setGraveyard(player1, List.of(new FieryImpulse()));
        setupAndCast();
        harness.setLibrary(player1, List.of(new FieryImpulse()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Fiery Impulse");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Spell mastery checks the graveyard at resolution rather than when cast")
    void spellMasteryChecksAtResolution() {
        harness.setGraveyard(player1, List.of(new FieryImpulse()));
        setupAndCast();
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new FieryImpulse()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
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

        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new YevasForcemage()));
    }
}
