package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelentlessRats.class, RagingGoblin.class, GrizzlyBears.class})
class RelentlessRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Relentless Rats is 2/2 when no other Relentless Rats are on the battlefield")
    void isBaseStatsWithNoOtherRats() {
        Permanent rats = addCreatureReady(player1, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Relentless Rats gets +1/+1 for each other Relentless Rats you control")
    void countsOwnOtherRats() {
        Permanent rats = addCreatureReady(player1, new RelentlessRats());
        harness.addToBattlefield(player1, new RelentlessRats());
        harness.addToBattlefield(player1, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Relentless Rats counts other Relentless Rats controlled by opponents too")
    void countsOpponentRatsToo() {
        Permanent rats = addCreatureReady(player1, new RelentlessRats());
        harness.addToBattlefield(player2, new RelentlessRats());
        harness.addToBattlefield(player2, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Relentless Rats does not count creatures with different names")
    void ignoresDifferentNames() {
        Permanent rats = addCreatureReady(player1, new RelentlessRats());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Relentless Rats bonus updates when other Relentless Rats leave the battlefield")
    void bonusUpdatesWhenOtherRatsLeave() {
        Permanent rats = addCreatureReady(player1, new RelentlessRats());
        harness.addToBattlefield(player1, new RelentlessRats());
        harness.addToBattlefield(player2, new RelentlessRats());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Relentless Rats"));

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(3);
    }
}
