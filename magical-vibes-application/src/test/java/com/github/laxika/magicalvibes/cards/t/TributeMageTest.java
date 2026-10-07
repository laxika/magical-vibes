package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.e.ExpeditionMap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LesserMasticore;
import com.github.laxika.magicalvibes.cards.s.ScrapyardRecombiner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributeMage.class, CopperMyr.class, ExpeditionMap.class, GrizzlyBears.class,
        LesserMasticore.class, ScrapyardRecombiner.class})
class TributeMageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates an optional search prompt")
    void etbCreatesMaySearchPrompt() {
        castTributeMage();

        resolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the search offers only artifact cards with mana value 2")
    void acceptingSearchOffersMatchingArtifacts() {
        setLibrary(new CopperMyr(), new ExpeditionMap(), new GrizzlyBears());
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Copper Myr");
    }

    @Test
    @DisplayName("Choosing an eligible artifact puts it into hand")
    void choosingEligibleArtifactPutsItIntoHand() {
        CopperMyr copperMyr = new CopperMyr();
        setLibrary(copperMyr);
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(copperMyr);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search does not search")
    void decliningSearchDoesNotSearch() {
        CopperMyr copperMyr = new CopperMyr();
        setLibrary(copperMyr);
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copperMyr);
    }

    @Test
    @DisplayName("No eligible artifact ends the search without prompting")
    void noEligibleArtifactEndsSearch() {
        setLibrary(new ExpeditionMap(), new GrizzlyBears());
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Artifacts above mana value 2 are excluded from the search")
    void artifactsAboveTwoAreExcluded() {
        LesserMasticore eligible = new LesserMasticore();
        setLibrary(new ScrapyardRecombiner(), eligible);
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(eligible);
    }

    @Test
    @DisplayName("The search may fail to find even when an eligible artifact is present")
    void mayFailToFindEligibleArtifact() {
        LesserMasticore eligible = new LesserMasticore();
        setLibrary(eligible);
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Searching an empty library finishes without a card choice")
    void emptyLibraryFinishesSearch() {
        setLibrary();
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The chosen artifact is revealed and only the controller's library is searched")
    void chosenArtifactIsRevealedFromControllersLibrary() {
        LesserMasticore chosen = new LesserMasticore();
        ScrapyardRecombiner remaining = new ScrapyardRecombiner();
        LesserMasticore opponentsCard = new LesserMasticore();
        setLibrary(chosen, remaining);
        harness.setLibrary(player2, List.of(opponentsCard));
        castTributeMage();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gameLogContains("reveals Lesser Masticore")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castTributeMage() {
        harness.setHand(player1, List.of(new TributeMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
