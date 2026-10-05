package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.cards.l.LogicKnot;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.p.PactOfTheTitan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheFuture.class, HorizonCanopy.class, NessianCourser.class, PactOfTheTitan.class, LogicKnot.class})
class MagusOfTheFutureTest extends BaseCardTest {
    @Test
    @DisplayName("A delve spell may be cast from the library by paying entirely with mana")
    void castsDelveSpellWithoutExilingCards() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        LogicKnot knot = new LogicKnot();
        PactOfTheTitan pact = new PactOfTheTitan();
        harness.setLibrary(player1, List.of(knot));
        harness.setHand(player2, List.of(pact));
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromLibraryTop(player1, pact.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(knot.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casting from the library still requires the spell's mana cost")
    void cannotCastCreatureWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        NessianCourser courser = new NessianCourser();
        harness.setLibrary(player1, List.of(courser));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(courser);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The permission does not let a creature spell be cast on an opponent's turn")
    void creatureStillRequiresNormalTiming() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        NessianCourser courser = new NessianCourser();
        harness.setLibrary(player1, List.of(courser));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(courser);
    }

    @Test
    @DisplayName("An instant on top may be cast on an opponent's turn")
    void castsInstantOnOpponentsTurn() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        harness.setLibrary(player1, List.of(new PactOfTheTitan()));
        harness.forceActivePlayer(player2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Pact of the Titan");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Playing a library land does not grant an additional land play")
    void libraryLandUsesNormalLandAllowance() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        HorizonCanopy first = new HorizonCanopy();
        HorizonCanopy second = new HorizonCanopy();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Horizon Canopy")).isEqualTo(1);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can cast successive top cards in the same turn")
    void castsSuccessiveTopCards() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        harness.setLibrary(player1, List.of(new NessianCourser(), new NessianCourser()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Nessian Courser")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }


    @Test
    @DisplayName("The controller may play a land from the top of their library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        HorizonCanopy land = new HorizonCanopy();
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Horizon Canopy");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may cast a creature spell from the top of their library")
    void castsCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        NessianCourser courser = new NessianCourser();
        harness.setLibrary(player1, List.of(courser));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Nessian Courser");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(courser);
    }

    @Test
    @DisplayName("The controller may cast an instant spell from the top of their library")
    void castsInstantFromLibraryTop() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        PactOfTheTitan pact = new PactOfTheTitan();
        harness.setLibrary(player1, List.of(pact));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Pact of the Titan");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(pact);
    }

    @Test
    @DisplayName("The top-library permissions apply only to the Magus's controller")
    void permissionsOnlyApplyToController() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        HorizonCanopy land = new HorizonCanopy();
        harness.setLibrary(player2, List.of(land));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(land);
    }

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsControllerTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        harness.setLibrary(player1, List.of(new PactOfTheTitan()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Pact of the Titan\""));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Pact of the Titan\""));
    }

    @Test
    @DisplayName("Losing all abilities removes the land-play permission")
    void losingAllAbilitiesRemovesLandPlayPermission() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheFuture());
        magus.setLosesAllAbilitiesUntilEndOfTurn(true);
        HorizonCanopy land = new HorizonCanopy();
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Losing all abilities removes the spell-casting permission")
    void losingAllAbilitiesRemovesSpellCastingPermission() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheFuture());
        magus.setLosesAllAbilitiesUntilEndOfTurn(true);
        NessianCourser courser = new NessianCourser();
        harness.setLibrary(player1, List.of(courser));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(courser);
    }

    @Test
    @DisplayName("Losing all abilities stops the top library card reveal")
    void losingAllAbilitiesStopsTopCardReveal() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheFuture());
        magus.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLibrary(player1, List.of(new PactOfTheTitan()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("\"name\":\"Pact of the Titan\""));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("\"name\":\"Pact of the Titan\""));
    }
}
