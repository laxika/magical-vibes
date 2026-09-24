package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainRipleyVance.class, LightningBolt.class})
class CaptainRipleyVanceTest extends BaseCardTest {

    @Test
    @DisplayName("The third spell puts a counter on Captain Ripley Vance and deals damage equal to its power")
    void thirdSpellPutsCounterAndDealsPowerDamage() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        castBoltAndResolve();
        castBoltAndResolve();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("The first two spells do not trigger Captain Ripley Vance")
    void firstTwoSpellsDoNotTrigger() {
        Permanent ripley = addCreatureReady(player1, new CaptainRipleyVance());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        castBoltAndResolve();
        castBoltAndResolve();

        assertThat(ripley.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void castBoltAndResolve() {
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
