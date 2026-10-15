package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceTheLivingGuildpact.class, Forest.class, Island.class, Shock.class, GrizzlyBears.class})
class JaceTheLivingGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts one of the top two cards into its owner's graveyard")
    void plusOnePutsOneOfTopTwoCardsIntoGraveyard() {
        Permanent jace = addReadyJace(player1);
        Card forest = new Forest();
        Card island = new Island();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, island, shock));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest, island);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, shock);
    }

    @Test
    @DisplayName("-3 returns another target nonland permanent to its owner's hand")
    void minusThreeReturnsAnotherNonlandPermanent() {
        Permanent jace = addReadyJace(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-3 cannot target a land or Jace himself")
    void minusThreeCannotTargetLandOrJace() {
        Permanent jace = addReadyJace(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, jace.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 shuffles each hand and graveyard into its library, then draws seven")
    void minusEightShufflesHandsAndGraveyardsThenDrawsSeven() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        Card ownHand = new Shock();
        Card ownGraveyard = new Forest();
        Card opponentHand = new Island();
        Card opponentGraveyard = new GrizzlyBears();
        harness.setHand(player1, List.of(ownHand));
        harness.setHand(player2, List.of(opponentHand));
        gd.playerGraveyards.get(player1.getId()).add(ownGraveyard);
        gd.playerGraveyards.get(player2.getId()).add(opponentGraveyard);
        harness.setLibrary(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).contains(ownHand)
                || gd.playerDecks.get(player1.getId()).contains(ownHand)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).contains(ownGraveyard)
                || gd.playerDecks.get(player1.getId()).contains(ownGraveyard)).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).contains(opponentHand, opponentGraveyard);
        harness.assertNotOnBattlefield(player1, "Jace, the Living Guildpact");
    }

    private Permanent addReadyJace(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceTheLivingGuildpact());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    @Test
    @DisplayName("+1 puts the only card in the library into the graveyard")
    void plusOneWithOneCardInLibrary() {
        addReadyJace(player1);
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("+1 with an empty library gains loyalty without requiring a choice")
    void plusOneWithEmptyLibrary() {
        Permanent jace = addReadyJace(player1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-3 can return an opposing planeswalker")
    void minusThreeReturnsAnotherPlaneswalker() {
        addReadyJace(player1);
        Permanent opposingJace = harness.addToBattlefieldAndReturn(player2, new JaceTheLivingGuildpact());
        opposingJace.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 1, null, opposingJace.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace, the Living Guildpact");
        assertThat(gd.playerHands.get(player2.getId())).contains(opposingJace.getCard());
        harness.assertOnBattlefield(player1, "Jace, the Living Guildpact");
    }

    @Test
    @DisplayName("-3 returns a stolen permanent to its owner rather than its controller")
    void minusThreeReturnsStolenPermanentToOwner() {
        addReadyJace(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears.getCard());
    }

    @Test
    @DisplayName("-8 leaves surviving permanents on the battlefield and empties both graveyards")
    void minusEightPreservesBattlefield() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 9);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Island()));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Jace, the Living Guildpact");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("-8 shuffles Jace himself into the library when the loyalty cost puts him in the graveyard")
    void minusEightShufflesJaceAfterZeroLoyalty() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(
                new Island(), new Island(), new Island(), new Island(), new Island(), new Island()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jace, the Living Guildpact");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).contains(jace.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
