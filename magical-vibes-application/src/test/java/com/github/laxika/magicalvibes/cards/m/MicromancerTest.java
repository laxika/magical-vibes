package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdventurousImpulse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Micromancer.class, Shock.class, AdventurousImpulse.class, LlanowarElves.class,
        GrizzlyBears.class, ThinkTwice.class})
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
        harness.handleCardChosen(player1, 0);

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

    @Test
    @DisplayName("A higher mana value instant is excluded from the search")
    void higherManaValueInstantIsExcluded() {
        setLibrary(new ThinkTwice());
        castMicromancer();
        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The controller can fail to find even when a matching card exists")
    void canFailToFindMatchingCard() {
        Shock shock = new Shock();
        setLibrary(shock);
        castMicromancer();
        resolveMay(true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A found sorcery is revealed and the remaining library is shuffled")
    void foundSorceryIsRevealedAndLibraryIsShuffled() {
        AdventurousImpulse impulse = new AdventurousImpulse();
        LlanowarElves elves = new LlanowarElves();
        setLibrary(impulse, elves);
        castMicromancer();
        resolveMay(true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(impulse);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Adventurous Impulse")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Accepting with an empty library completes the ability and shuffles")
    void emptyLibraryCompletesAbility() {
        setLibrary();
        castMicromancer();
        resolveMay(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Declining leaves the library in order and does not shuffle")
    void decliningLeavesLibraryUnchanged() {
        ThinkTwice first = new ThinkTwice();
        Micromancer second = new Micromancer();
        setLibrary(first, second);
        castMicromancer();
        resolveMay(false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isFalse();
    }

    private void castMicromancer() {
        harness.setHand(player1, List.of(new Micromancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveMay(boolean choice) {
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
