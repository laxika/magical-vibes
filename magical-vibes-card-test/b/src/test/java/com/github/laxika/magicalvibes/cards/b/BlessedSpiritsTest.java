package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlessedSpirits.class, IntangibleVirtue.class, GrizzlyBears.class})
class BlessedSpiritsTest extends BaseCardTest {

    private Permanent addSpirits(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BlessedSpirits());
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Casting an enchantment puts a +1/+1 counter on Blessed Spirits")
    void enchantmentCastAddsCounter() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters accumulate across multiple enchantment casts")
    void multipleEnchantmentCastsStackCounters() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        setUpMainPhase(player1);
        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a non-enchantment spell adds no counter")
    void creatureCastAddsNoCounter() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's enchantment cast does not trigger the ability")
    void opponentEnchantmentCastAddsNoCounter() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player2);

        harness.castFromHand(player2, new IntangibleVirtue(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter trigger resolves before the enchantment spell")
    void counterIsAddedBeforeEnchantmentResolves() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(spirits);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Blessed Spirits gets its own counter from one enchantment cast")
    void multipleSpiritsTriggerIndependently() {
        Permanent first = addSpirits(player1);
        Permanent second = addSpirits(player1);
        setUpMainPhase(player1);

        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not add a counter")
    void enchantmentEnteringWithoutCastDoesNotTrigger() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.enterBattlefieldAndReturn(player1, new IntangibleVirtue());

        assertThat(gd.stack).isEmpty();
        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A pending trigger does not put a counter on a new Blessed Spirits")
    void departedSourceDoesNotPutCounterOnReplacement() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);
        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");

        gd.playerBattlefields.get(player1.getId()).remove(spirits);
        gd.playerGraveyards.get(player1.getId()).add(spirits.getCard());
        Permanent replacement = addSpirits(player1);
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }
}
