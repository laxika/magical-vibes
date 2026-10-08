package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CompassGnome;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WailOfTheForgotten.class, GrizzlyBears.class, Plains.class, Shock.class, CompassGnome.class})
class WailOfTheForgottenTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWail(0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void targetOpponentDiscardsACard() {
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        castWail(1, List.of(player2.getId()));
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void looksAtTopThreeAndPutsOneIntoHandAndTheRestIntoGraveyard() {
        Card first = new Shock();
        Card chosen = new GrizzlyBears();
        Card third = new Plains();
        harness.setLibrary(player1, List.of(first, chosen, third));
        castWail(2, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void allModesAreAvailableWithEightPermanentCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1},
                List.of(target.getId(), player2.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void cannotChooseMultipleModesBeforeDescendEight() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{0, 1}, List.of(target.getId(), player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetALandWithTheReturnMode() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(land.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void cannotDeclinePuttingOneLookedAtCardIntoHand() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));
        castWail(2, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allThreeModesResolveInPrintedOrderEvenIfDescendIsLostAfterCasting() {
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains(), new Plains()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CompassGnome());
        Card first = new Plains();
        Card chosen = new WailOfTheForgotten();
        Card third = new Plains();
        harness.setLibrary(player1, List.of(first, chosen, third));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(target.getId(), player2.getId()), List.of());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void sevenPermanentsAndASorceryDoNotEnableMultipleModes() {
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains(), new WailOfTheForgotten()));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsPermanentCardsDoNotCountForDescend() {
        harness.setGraveyard(player2, List.of(
                new Plains(), new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains(), new Plains()));
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTheCasterWithTheDiscardMode() {
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{1}, List.of(player1.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void looksAtOnlyTheTwoAvailableCards() {
        Card chosen = new Plains();
        Card other = new WailOfTheForgotten();
        harness.setLibrary(player1, List.of(chosen, other));
        castWail(2, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void putsTheOnlyAvailableLibraryCardIntoHand() {
        Card onlyCard = new Plains();
        harness.setLibrary(player1, List.of(onlyCard));
        castWail(2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotCauseALossFromTheLookMode() {
        harness.setLibrary(player1, List.of());
        castWail(2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WailOfTheForgotten);
    }

    @Test
    void discardModeCanResolveAgainstAnOpponentWithAnEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        castWail(1, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof WailOfTheForgotten);
    }

    private void castWail(int modeIndex, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WailOfTheForgotten()));
        addWailMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{modeIndex}, targetIds, List.of());
        harness.passBothPriorities();
    }

    private void addWailMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
