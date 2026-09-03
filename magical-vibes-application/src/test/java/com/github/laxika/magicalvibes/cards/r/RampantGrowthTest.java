package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampantGrowth.class, Plains.class, Forest.class, Island.class, CrystalVein.class})
class RampantGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rampant Growth puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Resolving Rampant Growth presents only basic lands and destination is battlefield tapped")
    void resolvingPresentsBasicLandsToBattlefieldTapped() {
        setupAndCast();
        List<Card> basicLands = setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrderElementsOf(basicLands);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters battlefield tapped")
    void chosenBasicLandEntersTapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        Card chosenCard = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(chosenCard.getId()) && p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosenCard);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player can fail to find with Rampant Growth")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolving with no basic lands does not prompt for library choice")
    void noBasicLandsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new RampantGrowth(), new RampantGrowth()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("finds no basic land cards")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Resolving with empty library does not prompt for library choice")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("it is empty")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private List<Card> setupLibrary() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(plains, forest, island, new CrystalVein(), new RampantGrowth()));
        return List.of(plains, forest, island);
    }
}
