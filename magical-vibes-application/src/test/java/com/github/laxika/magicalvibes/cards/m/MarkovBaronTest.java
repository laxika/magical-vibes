package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkovBaron.class, BaronyVampire.class, GrizzlyBears.class})
class MarkovBaronTest extends BaseCardTest {

    @Test
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent vampire = findPermanent(player1, "Barony Vampire");

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MarkovBaron());

        Permanent baron = findPermanent(player1, "Markov Baron");

        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(2);
    }

    @Test
    void doesNotBuffNonVampiresOrOpponentsVampires() {
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BaronyVampire());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentVampire = findPermanent(player2, "Barony Vampire");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
    }
}
