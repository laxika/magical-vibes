package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoldTheGates.class, GrizzlyBears.class, RakdosGuildgate.class, Opalescence.class})
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

    @Test
    @DisplayName("Multiple copies stack their bonuses for creatures entering later")
    void multipleCopiesAffectNewCreatures() {
        harness.addToBattlefield(player1, new HoldTheGates());
        harness.addToBattlefield(player1, new HoldTheGates());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The toughness bonus and vigilance end when Hold the Gates leaves")
    void effectsEndWhenSourceLeaves() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HoldTheGates());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An animated Hold the Gates grants itself vigilance even without Gates")
    void animatedSourceGrantsItselfVigilance() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HoldTheGates());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, enchantment)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantment, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An animated Hold the Gates receives its own Gate-based toughness bonus")
    void animatedSourceReceivesItsOwnBonus() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HoldTheGates());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.isCreature(gd, enchantment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(4);
    }
}
