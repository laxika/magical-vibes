package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdventurousImpulse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MicromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability offers only mana value one instants and sorceries")
    void acceptingMayOffersMatchingCards() {
        setLibrary(new Shock(), new AdventurousImpulse(), new LlanowarElves(), new GrizzlyBears());
        castMicromancer();

        resolveMay(true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Shock", "Adventurous Impulse");
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand")
    void choosingMatchingCardPutsItIntoHand() {
        setLibrary(new Shock(), new LlanowarElves());
        castMicromancer();
        resolveMay(true);

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMayDoesNotSearch() {
        setLibrary(new Shock());
        castMicromancer();

        resolveMay(false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No matching card means accepting the may ability finds nothing")
    void noMatchingCardFindsNothing() {
        setLibrary(new LlanowarElves(), new GrizzlyBears());
        castMicromancer();

        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castMicromancer() {
        harness.setHand(player1, List.of(new Micromancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMay(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
