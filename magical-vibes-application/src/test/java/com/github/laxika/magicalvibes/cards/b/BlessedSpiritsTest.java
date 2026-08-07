package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlessedSpiritsTest extends BaseCardTest {

    private Permanent addSpirits(Player player) {
        Permanent perm = new Permanent(new BlessedSpirits());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new IntangibleVirtue()));
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters accumulate across multiple enchantment casts")
    void multipleEnchantmentCastsStackCounters() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new IntangibleVirtue()));
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new IntangibleVirtue()));
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a non-enchantment spell adds no counter")
    void creatureCastAddsNoCounter() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's enchantment cast does not trigger the ability")
    void opponentEnchantmentCastAddsNoCounter() {
        Permanent spirits = addSpirits(player1);
        setUpMainPhase(player2);

        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new IntangibleVirtue()));
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(spirits.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
