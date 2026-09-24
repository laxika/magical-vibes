package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Spellseeker.class, Negate.class, Shock.class, Divination.class, GrizzlyBears.class})
class SpellseekerTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability offers instant and sorcery cards with mana value 2 or less")
    void offersEligibleSpells() {
        setLibrary(new Negate(), new Shock(), new Divination(), new GrizzlyBears());
        castSpellseeker();

        resolveEnterTheBattlefieldTrigger();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Negate", "Shock");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a matching spell puts it into hand and shuffles the library")
    void chosenSpellGoesToHand() {
        Negate negate = new Negate();
        setLibrary(negate, new GrizzlyBears());
        castSpellseeker();
        resolveEnterTheBattlefieldTrigger();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(negate);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(negate);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("No search interaction is created when the library has no matching spell")
    void noMatchingSpell() {
        setLibrary(new Divination(), new GrizzlyBears());
        castSpellseeker();

        resolveEnterTheBattlefieldTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("finds no")).isTrue();
    }

    private void castSpellseeker() {
        harness.setHand(player1, List.of(new Spellseeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTheBattlefieldTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
