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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

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
        Permanent perm = new Permanent(new JaceTheLivingGuildpact());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
