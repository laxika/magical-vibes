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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JaceTheMindSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("+2 looks at a target player's top card and can put it on the bottom")
    void plusTwoCanPutTargetPlayersTopCardOnBottom() {
        Permanent jace = addReadyJace(player1, 3);
        Card top = new Shock();
        Card next = new Forest();
        harness.setLibrary(player2, List.of(top, next));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next, top);
    }

    @Test
    @DisplayName("0 draws three cards and puts two chosen cards on top in order")
    void zeroDrawsThreeAndPutsTwoOnTop() {
        Permanent jace = addReadyJace(player1, 3);
        Card first = new Shock();
        Card second = new Forest();
        Card third = new Island();
        Card fourth = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, fourth);
    }

    @Test
    @DisplayName("-1 returns a target creature to its owner's hand")
    void minusOneReturnsTargetCreature() {
        Permanent jace = addReadyJace(player1, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-1 cannot target a land")
    void minusOneCannotTargetLand() {
        addReadyJace(player1, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-12 exiles the target library, then shuffles that player's hand into it")
    void minusTwelveExilesLibraryAndShufflesHandIntoIt() {
        Permanent jace = addReadyJace(player1, 12);
        Card libraryCard = new Shock();
        Card secondLibraryCard = new Forest();
        Card handCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard, secondLibraryCard));
        harness.setHand(player2, List.of(handCard));

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(libraryCard, secondLibraryCard);
        harness.assertNotOnBattlefield(player1, "Jace, the Mind Sculptor");
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent perm = new Permanent(new JaceTheMindSculptor());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
