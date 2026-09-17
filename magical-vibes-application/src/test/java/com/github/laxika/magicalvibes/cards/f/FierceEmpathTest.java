package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FierceEmpath.class, ElvishAberration.class, GoblinBrigand.class, TempleOfTheFalseGod.class})
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

    private void setupAndCast() {
        harness.setHand(player1, List.of(new FierceEmpath()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
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
