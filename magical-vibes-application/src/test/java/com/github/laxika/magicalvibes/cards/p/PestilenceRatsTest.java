package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PestilenceRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Pestilence Rats is 0/3 with no other Rats")
    void zeroPowerWithNoOtherRats() {
        Permanent rats = addPestilenceRats(player1);

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats power equals other Rats you control")
    void powerEqualsOtherControlledRats() {
        Permanent rats = addPestilenceRats(player1);
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player1, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats counts opponent Rats")
    void countsOpponentRats() {
        Permanent rats = addPestilenceRats(player1);
        harness.addToBattlefield(player2, new TyphoidRats());
        harness.addToBattlefield(player2, new PestilenceRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats does not count non-Rat creatures")
    void doesNotCountNonRats() {
        Permanent rats = addPestilenceRats(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pestilence Rats power updates when other Rats leave")
    void powerUpdatesWhenRatsLeave() {
        Permanent rats = addPestilenceRats(player1);
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Typhoid Rats"));

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }

    private Permanent addPestilenceRats(Player player) {
        Permanent permanent = new Permanent(new PestilenceRats());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
