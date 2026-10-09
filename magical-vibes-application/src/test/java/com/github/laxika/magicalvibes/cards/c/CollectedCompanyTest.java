package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectedCompany.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Shock.class, Forest.class, TrollAscetic.class, GrafdiggersCage.class})
class CollectedCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two creature cards with mana value 3 or less")
    void offersEligibleCreaturesUpToTwo() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        setLibrary(bears, new HillGiant(), elves, new Shock(), new Forest(), new HillGiant());

        castCollectedCompany();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), elves.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .hasSize(4);
    }

    @Test
    @DisplayName("May put only one eligible creature onto the battlefield")
    void mayChooseFewerThanTwo() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        setLibrary(bears, new Shock(), elves, new Forest());

        castCollectedCompany();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .hasSize(3);
    }

    @Test
    @DisplayName("May decline all eligible creatures")
    void mayChooseNoCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new Shock(), new Forest());

        castCollectedCompany();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() == bears);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .hasSize(3);
    }

    @Test
    @DisplayName("Looks at only six cards and puts the rest below the untouched library in chosen order")
    void looksAtSixAndOrdersRemainderOnBottom() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        HillGiant giant = new HillGiant();
        Shock shock = new Shock();
        Forest forest = new Forest();
        HillGiant secondGiant = new HillGiant();
        GrizzlyBears seventh = new GrizzlyBears();
        setLibrary(bears, giant, elves, shock, forest, secondGiant, seventh);

        castCollectedCompany();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactly(bears.getId(), elves.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(seventh, secondGiant, forest, shock, giant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Creature cards with mana value exactly three can enter without being cast")
    void acceptsManaValueThree() {
        TrollAscetic troll = new TrollAscetic();
        Forest forest = new Forest();
        setLibrary(troll, forest);

        castCollectedCompany();
        harness.handleMultipleCardsChosen(player1, List.of(troll.getId()));

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible creatures all looked-at cards go to the bottom in chosen order")
    void noEligibleCreatures() {
        HillGiant giant = new HillGiant();
        Shock shock = new Shock();
        Forest forest = new Forest();
        setLibrary(giant, shock, forest);

        castCollectedCompany();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        var remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                remaining.indexOf(forest), remaining.indexOf(giant), remaining.indexOf(shock))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, giant, shock);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice")
    void emptyLibrary() {
        setLibrary();

        castCollectedCompany();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Collected Company");
    }

    @Test
    @DisplayName("Grafdigger's Cage leaves blocked creatures among the cards to order on the bottom")
    void blockedCreaturesGoToBottomWithRemainder() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Forest forest = new Forest();
        setLibrary(bears, shock, forest);

        castCollectedCompany();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrder(bears, shock, forest);
        var remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                remaining.indexOf(forest), remaining.indexOf(bears), remaining.indexOf(shock))));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bears, shock);
    }

    private void castCollectedCompany() {
        harness.setHand(player1, List.of(new CollectedCompany()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
