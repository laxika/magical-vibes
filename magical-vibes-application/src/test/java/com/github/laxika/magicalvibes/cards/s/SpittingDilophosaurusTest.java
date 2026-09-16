package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpittingDilophosaurus.class, GrizzlyBears.class})
class SpittingDilophosaurusTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on up to one target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpittingDilophosaurus()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking puts a -1/-1 counter on up to one target creature")
    void attackPutsCounterOnTargetCreature() {
        addCreatureReady(player1, new SpittingDilophosaurus());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent creatures with -1/-1 counters can't block")
    void opponentMinusOneMinusOneCounterCreatureCannotBlock() {
        addCreatureReady(player1, new SpittingDilophosaurus());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent counteredBlocker = addCreatureReady(player2, new GrizzlyBears());
        counteredBlocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent plusOneBlocker = addCreatureReady(player2, new GrizzlyBears());
        plusOneBlocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ownCounteredBlocker = addCreatureReady(player1, new GrizzlyBears());
        ownCounteredBlocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(bls.canBlockAttacker(gd, counteredBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, plusOneBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, ownCounteredBlocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("An opponent creature with a -1/-1 counter can still attack")
    void opponentMinusOneMinusOneCounterCreatureCanAttack() {
        addCreatureReady(player1, new SpittingDilophosaurus());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        declareAttackers(player2, List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }
}
