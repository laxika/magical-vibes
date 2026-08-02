package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HoldTheGatesTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures have vigilance even when you control no Gates")
    void vigilanceWithoutGates() {
        harness.addToBattlefield(player1, new HoldTheGates());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Gate you control gives your creatures +0/+1")
    void toughnessScalesWithControlledGates() {
        harness.addToBattlefield(player1, new HoldTheGates());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent-controlled Gates do not affect your creatures")
    void opponentGatesDoNotCount() {
        harness.addToBattlefield(player1, new HoldTheGates());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The bonus updates when a controlled Gate leaves")
    void bonusUpdatesWhenGateLeaves() {
        harness.addToBattlefield(player1, new HoldTheGates());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Rakdos Guildgate"));

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
