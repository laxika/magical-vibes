package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

class SarkhanDragonsoulTest extends BaseCardTest {

    @Test
    @DisplayName("+2 deals 1 damage to each opponent and each creature your opponents control")
    void plusTwoDamagesOpponentsAndTheirCreatures() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);
        Permanent opponentPlaneswalker = addReadyPlaneswalker(player2, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("-3 deals 4 damage to a target player")
    void minusThreeDamagesTargetPlayer() {
        Permanent sarkhan = addReadySarkhan(player1, 5);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 deals 4 damage to a target planeswalker")
    void minusThreeDamagesTargetPlaneswalker() {
        addReadySarkhan(player1, 5);
        Permanent target = addReadyPlaneswalker(player2, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-9 puts any number of Dragon creature cards from the library onto the battlefield")
    void minusNinePutsDragonsOntoBattlefield() {
        addReadySarkhan(player1, 9);
        harness.setLibrary(player1, List.of(new DragonEgg(), new SarkhanFireblood(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards()).allMatch(card ->
                card.hasType(CardType.CREATURE) && card.getSubtypes().contains(CardSubtype.DRAGON));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Dragon Egg"));
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent permanent = new Permanent(new SarkhanDragonsoul());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addCreature(Player player) {
        harness.addToBattlefield(player, new GrizzlyBears());
        return findPermanent(player, "Grizzly Bears");
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = new Permanent(new SarkhanFireblood());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
