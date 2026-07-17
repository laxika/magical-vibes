package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KreshTheBloodbraidedTest extends BaseCardTest {

    private Permanent kresh() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Accepting the may puts X +1/+1 counters equal to the dying creature's power")
    void putsCountersEqualToDyingPowerOnAccept() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());

        harness.passBothPriorities(); // Shock resolves → Bears dies → Kresh trigger on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may puts no counters")
    void noCountersOnDecline() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters equal the dying creature's last-known effective power (with +1/+1 counters)")
    void usesEffectivePowerOfDyingCreature() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 2/2 Grizzly Bears becomes 3/3

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());

        harness.passBothPriorities(); // DoomBlade resolves → Bears dies → Kresh trigger on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
