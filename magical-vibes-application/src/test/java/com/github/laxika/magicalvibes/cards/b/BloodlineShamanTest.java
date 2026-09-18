package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodlineShaman.class, AvianChangeling.class, ElvishWarrior.class, Forest.class,
        GluttonousZombie.class})
class BloodlineShamanTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card of the chosen type goes into its controller's hand")
    void matchingCreatureGoesToHand() {
        ElvishWarrior elf = new ElvishWarrior();
        activateAndChoose(elf, "ELF");

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elf);
    }

    @Test
    @DisplayName("A nonmatching creature card goes into its controller's graveyard")
    void nonmatchingCreatureGoesToGraveyard() {
        GluttonousZombie zombie = new GluttonousZombie();
        activateAndChoose(zombie, "ELF");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(zombie);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(zombie);
    }

    @Test
    @DisplayName("A noncreature card goes into its controller's graveyard")
    void noncreatureGoesToGraveyard() {
        Forest forest = new Forest();
        activateAndChoose(forest, "BEAR");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("A Changeling creature card matches the chosen type")
    void changelingMatchesChosenType() {
        AvianChangeling changeling = new AvianChangeling();
        activateAndChoose(changeling, "ELF");

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(changeling);
    }

    @Test
    @DisplayName("Only the top card is revealed and moved")
    void onlyTopCardIsProcessed() {
        ElvishWarrior topCard = new ElvishWarrior();
        Forest cardBelowTop = new Forest();
        activateAndChoose(List.of(topCard, cardBelowTop), "ELF");

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBelowTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An empty library is left empty")
    void emptyLibraryDoesNothing() {
        activateAndChoose(List.of(), "ELF");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void activateAndChoose(Card topCard, String subtype) {
        activateAndChoose(List.of(topCard), subtype);
    }

    private void activateAndChoose(List<Card> library, String subtype) {
        addCreatureReady(player1, new BloodlineShaman());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }
}
