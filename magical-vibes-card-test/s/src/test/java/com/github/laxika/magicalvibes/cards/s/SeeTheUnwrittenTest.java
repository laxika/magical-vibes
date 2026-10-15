package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeeTheUnwritten.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        Shock.class})
class SeeTheUnwrittenTest extends BaseCardTest {

    @Test
    @DisplayName("Without Ferocious, may put one creature onto the battlefield")
    void withoutFerociousPutsOneCreatureOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        setLibrary(bears, giant, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolve();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), giant.getId());

        harness.handleCardChosen(player1, choice.params().cards().indexOf(bears));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(giant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ferocious allows putting two creature cards onto the battlefield")
    void ferociousPutsTwoCreaturesOntoBattlefield() {
        harness.addToBattlefield(player1, new AirElemental());
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        setLibrary(bears, giant, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bears)
                .anyMatch(permanent -> permanent.getCard() == giant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With no creature cards revealed, all eight cards go to the graveyard")
    void noCreaturesPutsAllRevealedCardsIntoGraveyard() {
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
    }

    @Test
    void mayDeclineWithoutFerocious() {
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        setLibrary(bears, giant);

        castAndResolve();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, giant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayChooseOnlyOneCreatureWithFerocious() {
        harness.addToBattlefield(player1, new AirElemental());
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        setLibrary(bears, giant);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bears)
                .noneMatch(permanent -> permanent.getCard() == giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(giant);
    }

    @Test
    void mayDeclineAllCreaturesWithFerocious() {
        harness.addToBattlefield(player1, new AirElemental());
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        setLibrary(bears, giant);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, giant);
    }

    @Test
    void revealedFourPowerCreatureDoesNotEnableFerocious() {
        Card elemental = new AirElemental();
        Card bears = new GrizzlyBears();
        setLibrary(elemental, bears);

        castAndResolve();
        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, choice.params().cards().indexOf(elemental));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .anyMatch(permanent -> permanent.getCard() == elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }

    @Test
    void opponentsFourPowerCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player2, new AirElemental());
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        setLibrary(bears, giant);

        castAndResolve();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, giant);
    }

    @Test
    void onlyTopEightCardsAreMoved() {
        Card ninth = new GrizzlyBears();
        setLibrary(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock(), ninth);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ninth);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutChoice() {
        setLibrary();

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof SeeTheUnwritten);
    }

    @Test
    @CardUsed({LeylineOfTheVoid.class})
    void noEligibleCreaturesStillAppliesGraveyardReplacement() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Card first = new Shock();
        Card second = new Shock();
        setLibrary(first, second);

        castAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SeeTheUnwritten()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
