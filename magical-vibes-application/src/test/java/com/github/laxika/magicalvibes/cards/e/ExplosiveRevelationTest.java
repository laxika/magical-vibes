package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({ExplosiveRevelation.class, Forest.class, GideonJura.class, GrizzlyBears.class, HeatRay.class,
        Island.class, Shock.class})
class ExplosiveRevelationTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToFirstNonlandManaValueAndPutsItIntoHand() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsRevealedLandsOnBottomInChosenOrder() {
        Card forest = new Forest();
        Card island = new Island();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island, bears));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDealsNoDamage() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void dealsLethalDamageToCreatureAndStillPutsRevealedCardIntoHand() {
        Card revealed = new GrizzlyBears();
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(revealed));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void illegalTargetPreventsRevealingOrMovingLibraryCards() {
        Card land = new Forest();
        Card revealed = new GrizzlyBears();
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(land, revealed));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Explosive Revelation");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, revealed);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void singleRevealedLandGoesBelowUnrevealedCardsWithoutAnOrderPrompt() {
        Card land = new Forest();
        Card revealed = new GrizzlyBears();
        Card unrevealed = new Shock();
        harness.setLibrary(player1, List.of(land, revealed, unrevealed));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void libraryContainingOnlyLandsDealsNoDamageAndReturnsAllLandsInChosenOrder() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void revealedXSpellUsesZeroForXAndCanDamageItsController() {
        Card revealed = new HeatRay();
        harness.setLibrary(player1, List.of(revealed));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void dealsManaValueDamageToPlaneswalkerLoyalty() {
        var target = harness.enterBattlefieldAndReturn(player2, new GideonJura());
        Card revealed = new ExplosiveRevelation();
        harness.setLibrary(player1, List.of(revealed));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Gideon Jura");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    void orderedLandsGoBelowEveryUnrevealedCard() {
        Card forest = new Forest();
        Card island = new Island();
        Card revealed = new GrizzlyBears();
        Card unrevealed = new Shock();
        harness.setLibrary(player1, List.of(forest, island, revealed, unrevealed));
        harness.setHand(player1, List.of(new ExplosiveRevelation()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.indexOf(island), reorder.indexOf(forest))));

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, island, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
