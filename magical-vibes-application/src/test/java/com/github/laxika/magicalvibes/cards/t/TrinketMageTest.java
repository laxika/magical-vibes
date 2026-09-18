package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.g.GuardianIdol;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrinketMage.class, ConjurersBauble.class, GuardianIdol.class, ParadiseMantle.class,
        SparkElemental.class})
class TrinketMageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Trinket Mage creates may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        resolveEtb();

        harness.assertOnBattlefield(player1, "Trinket Mage");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability presents only artifacts with MV 1 or less")
    void acceptingMayPresentsOnlyLowMVArtifacts() {
        setupAndCast();
        setupLibrary();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.ARTIFACT)
                        && c.getManaValue() <= 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing an artifact puts it into hand")
    void choosingArtifactPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertInHand(player1, "Conjurer's Bauble");
    }

    @Test
    @DisplayName("Declining may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("searches their library")).isFalse();
    }

    @Test
    @DisplayName("Artifacts with MV 2 or more are excluded from search")
    void highMVArtifactsExcluded() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GuardianIdol(), new SparkElemental()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("finds no artifact cards with mana value 1 or less")).isTrue();
    }

    @Test
    @DisplayName("Non-artifact cards are excluded from search even if low MV")
    void nonArtifactsExcluded() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new SparkElemental(), new SparkElemental()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("finds no artifact cards with mana value 1 or less")).isTrue();
    }

    @Test
    @DisplayName("Player can fail to find with Trinket Mage")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new TrinketMage(), "{2}{U}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new ConjurersBauble(), new ParadiseMantle(), new GuardianIdol(), new SparkElemental()));
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
