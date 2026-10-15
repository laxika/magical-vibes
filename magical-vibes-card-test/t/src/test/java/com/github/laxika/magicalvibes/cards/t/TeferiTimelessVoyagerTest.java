package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiTimelessVoyager.class, GrizzlyBears.class, Plains.class})
class TeferiTimelessVoyagerTest extends BaseCardTest {

    @Test
    void plusOneDrawsACard() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawn);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusThreePutsTargetCreatureOnTopOfItsOwnersLibrary() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Permanent creature = addCreature(player2, new GrizzlyBears());
        Card libraryCard = new Plains();
        harness.setLibrary(player2, List.of(libraryCard));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature.getCard(), libraryCard);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusThreeCannotTargetAPlayerOrLand() {
        addReadyTeferi(player1, 4);
        Permanent land = new Permanent(new Plains());
        gd.playerBattlefields.get(player2.getId()).add(land);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusEightPhasesOutOnlyTargetOpponentsCreaturesUntilEndOfNextTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addReadyTeferi(player1, 8);
        Permanent creature = addCreature(player2, new GrizzlyBears());
        Permanent land = new Permanent(new Plains());
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(creature);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TeferiTimelessVoyager());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
