package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBookOfExaltedDeeds.class, GrizzlyBears.class})
class TheBookOfExaltedDeedsTest extends BaseCardTest {

    @Test
    void createsAngelAtEndStepAfterGainingThreeLife() {
        harness.addToBattlefield(player1, new TheBookOfExaltedDeeds());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isZero();
    }

    @Test
    void abilityExilesBookAndGrantsGameLockAbilityToTargetAngel() {
        addBookAndCreateAngel();
        Permanent angel = findPermanent(player1, "Angel");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, angel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Book of Exalted Deeds");
        assertThat(angel.getCounterCount(CounterType.ENLIGHTENED)).isEqualTo(1);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player1, 20);
        harness.setLife(player2, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void abilityRequiresAnAngelTarget() {
        addBookReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityIsSorcerySpeedOnly() {
        addBookReady(player1);
        Permanent angel = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addBookAndCreateAngel() {
        addBookReady(player1);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Angel");
    }

    private Permanent addBookReady(Player player) {
        Permanent book = new Permanent(new TheBookOfExaltedDeeds());
        book.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(book);
        return book;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
