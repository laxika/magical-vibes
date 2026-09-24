package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.e.ExpeditionMap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributeMage.class, CopperMyr.class, ExpeditionMap.class, GrizzlyBears.class})
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
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
