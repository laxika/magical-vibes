package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodhallOozeTest extends BaseCardTest {

    private Permanent addOoze(Player player) {
        Permanent perm = new Permanent(new BloodhallOoze());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void advanceToUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger(s) go on stack
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when controlling a black permanent and accepting")
    void counterFromBlackPermanent() {
        Permanent ooze = addOoze(player1);
        addPermanent(player1, new ScatheZombies());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> queues may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when controlling a green permanent and accepting")
    void counterFromGreenPermanent() {
        Permanent ooze = addOoze(player1);
        addPermanent(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> queues may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both triggers fire when controlling a black and a green permanent")
    void twoCountersFromBothColors() {
        Permanent ooze = addOoze(player1);
        addPermanent(player1, new ScatheZombies());
        addPermanent(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve first trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve second trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when declining the may")
    void noCounterWhenDeclining() {
        Permanent ooze = addOoze(player1);
        addPermanent(player1, new ScatheZombies());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> queues may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger without a black or green permanent (intervening if)")
    void noTriggerWithoutBlackOrGreen() {
        Permanent ooze = addOoze(player1);
        // Only the red Ooze itself is controlled — neither black nor green.

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
