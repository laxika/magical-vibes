package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExplosiveProdigy;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanarInnovativeFirstYear.class, ExplosiveProdigy.class, Island.class})
class SanarInnovativeFirstYearTest extends BaseCardTest {

    @Test
    void revealsThroughLandsAndExilesOneCardForEachColor() {
        Card blue = new SanarInnovativeFirstYear();
        Card red = new ExplosiveProdigy();
        Card land = new Island();
        Card unrevealed = new ExplosiveProdigy();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(land, blue, red, unrevealed));

        trigger();
        harness.passBothPriorities();
        assertThat(choice().validCardIds()).containsExactly(blue.getId());
        harness.handleMultipleCardsChosen(player1, List.of(blue.getId()));
        assertThat(choice().validCardIds()).containsExactly(red.getId());
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));

        assertThat(gd.findExiledCard(blue.getId())).isNotNull();
        assertThat(gd.findExiledCard(red.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, unrevealed);
    }

    @Test
    void mayDeclineEveryColorAndReturnsAllRevealedCards() {
        Card blue = new SanarInnovativeFirstYear();
        Card red = new ExplosiveProdigy();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(blue, red));
        trigger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(blue.getId())).isNull();
        assertThat(gd.findExiledCard(red.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(blue, red);
    }

    @Test
    void multicolorCardCannotBeExiledAgainForAnotherColor() {
        Card multicolor = new SanarInnovativeFirstYear();
        Card red = new ExplosiveProdigy();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(multicolor, red));
        trigger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(multicolor.getId()));

        assertThat(choice().validCardIds()).containsExactly(red.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(multicolor.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        assertThat(gd.findExiledCard(multicolor.getId())).isNotNull();
    }

    @Test
    void handlesLibraryWithFewerNonlandsThanColors() {
        Card red = new ExplosiveProdigy();
        Card land = new Island();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(land, red));
        trigger();
        harness.passBothPriorities();
        assertThat(choice().validCardIds()).containsExactly(red.getId());
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));

        assertThat(gd.findExiledCard(red.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void countsColorsOnResolutionAndIgnoresOpponentPermanentsAndColorlessLands() {
        Card red = new ExplosiveProdigy();
        Card unrevealed = new SanarInnovativeFirstYear();
        var sanar = harness.addToBattlefieldAndReturn(player1, new SanarInnovativeFirstYear());
        harness.addToBattlefield(player1, new ExplosiveProdigy());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(red, unrevealed));
        trigger();
        gd.playerBattlefields.get(player1.getId()).remove(sanar);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));

        assertThat(gd.findExiledCard(red.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed);
    }

    @Test
    void zeroColorsRevealsNothingAndCreatesNoChoice() {
        Card top = new ExplosiveProdigy();
        var sanar = harness.addToBattlefieldAndReturn(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(top));
        trigger();
        gd.playerBattlefields.get(player1.getId()).remove(sanar);
        harness.passBothPriorities();

        assertThat(choice()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.findExiledCard(top.getId())).isNull();
    }

    @Test
    void exiledCardRequiresNormalManaAndCanBeCastThisTurn() {
        Card red = new ExplosiveProdigy();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(red));
        trigger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, red.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, red.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Explosive Prodigy");
        assertThat(gd.findExiledCard(red.getId())).isNull();
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of());
        trigger();
        harness.passBothPriorities();

        assertThat(choice()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castingPermissionDoesNotOverrideCreatureTiming() {
        Card red = new ExplosiveProdigy();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(red));
        trigger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, red.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(red.getId())).isNotNull();
    }

    @Test
    void permissionExpiresAfterTheTurnWithoutReturningTheCard() {
        Card red = new ExplosiveProdigy();
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.setLibrary(player1, List.of(red));
        trigger();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(red.getId())).isNotNull();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, red.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTriggerOnOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new SanarInnovativeFirstYear());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(choice()).isNull();
    }

    private void trigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);
    }

    private PendingInteraction.VividCardChoice choice() {
        return gd.interaction.activeInteraction(PendingInteraction.VividCardChoice.class);
    }
}
