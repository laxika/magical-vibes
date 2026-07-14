package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HateflayerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{R} and untapping deals 5 (its power) damage to target player")
    void dealsPowerDamageToPlayer() {
        Permanent hateflayer = addTapped(player1, new Hateflayer()); // 5/5
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Paying {Q} untapped the source.
        assertThat(hateflayer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Wither: damage to a creature is dealt as -1/-1 counters, killing a 2/2")
    void witherDealsMinusCountersToCreature() {
        addTapped(player1, new Hateflayer()); // 5/5, wither
        Permanent bears = addReady(player2, new GrizzlyBears()); // 2/2
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        UUID targetId = bears.getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(bears.getMarkedDamage()).isEqualTo(0);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate while untapped ({Q} requires the source to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new Hateflayer());
        harness.addMana(player1, ManaColor.RED, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
