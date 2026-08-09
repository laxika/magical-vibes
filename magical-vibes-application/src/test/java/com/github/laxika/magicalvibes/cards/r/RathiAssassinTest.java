package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RathiAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("First ability destroys a tapped nonblack creature")
    void destroysTappedNonblackCreature() {
        addRathiAssassin();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();
        addBlackManaCost();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("First ability rejects untapped and black creatures")
    void firstAbilityRejectsIllegalTargets() {
        addRathiAssassin();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addBlackManaCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped nonblack creature");

        Permanent imp = addCreatureReady(player2, new BogImp());
        imp.tap();
        addBlackManaCost();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, imp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped nonblack creature");
    }

    @Test
    @DisplayName("Second ability puts a qualifying Mercenary permanent onto the battlefield")
    void searchesMercenaryPermanentWithManaValueAtMostThree() {
        addRathiAssassin();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new DauthiMercenary(), new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Dauthi Mercenary");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Dauthi Mercenary");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    private void addRathiAssassin() {
        addCreatureReady(player1, new RathiAssassin());
    }

    private void addBlackManaCost() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
