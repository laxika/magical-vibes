package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.d.DecreeOfPain;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FierceEmpath.class, ElvishAberration.class, GoblinBrigand.class, TempleOfTheFalseGod.class,
        DecreeOfPain.class})
class FierceEmpathTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB may ability only offers creature cards with mana value 6 or greater")
    void acceptingMayOffersMatchingCreatures() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Elvish Aberration");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind())
                .isTrue();
    }

    @Test
    @DisplayName("Choosing a matching creature puts it into hand")
    void choosingMatchingCreaturePutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(true);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Elvish Aberration");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the ETB may ability with no matching creature finds nothing and shuffles")
    void acceptingMayWithNoMatchingCreatureFindsNothing() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GoblinBrigand(), new TempleOfTheFalseGod()));

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Goblin Brigand", "Temple of the False God");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Goblin Brigand", "Temple of the False God");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNull();
    }

    @Test
    @DisplayName("A noncreature with mana value above six cannot be found")
    void excludesHighManaValueNoncreatures() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new DecreeOfPain(), new ElvishAberration(), new FierceEmpath()));

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Elvish Aberration");
    }

    @Test
    @DisplayName("The controller can fail to find even when a qualifying creature exists")
    void canFailToFindMatchingCreature() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Elvish Aberration");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Elvish Aberration", "Goblin Brigand", "Temple of the False God");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Accepting the search with an empty library still shuffles")
    void emptyLibraryStillShuffles() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Declining the search preserves the library and does not shuffle")
    void decliningSearchDoesNotShuffle() {
        setupAndCast();
        setupLibrary();

        resolveMayAbility(false);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elvish Aberration", "Goblin Brigand", "Temple of the False God");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isFalse();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new FierceEmpath(), "{2}{G}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new ElvishAberration(), new GoblinBrigand(), new TempleOfTheFalseGod()));
    }

    private void resolveMayAbility(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
