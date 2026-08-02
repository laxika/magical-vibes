package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CruelSadistTest extends BaseCardTest {

    @Test
    @DisplayName("First ability pays life, taps, and puts a +1/+1 counter on this creature")
    void firstAbility() {
        Permanent sadist = addReadySadist(player1, 0);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(sadist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability removes the chosen number of counters and deals that much damage")
    void secondAbilityUsesChosenCounterAmount() {
        Permanent sadist = addReadySadist(player1, 3);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = bears.getId();
        harness.activateAbility(player1, 0, 1, null, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadySadist(Player player, int counters) {
        Permanent sadist = new Permanent(new CruelSadist());
        sadist.setSummoningSick(false);
        sadist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(sadist);
        return sadist;
    }
}
