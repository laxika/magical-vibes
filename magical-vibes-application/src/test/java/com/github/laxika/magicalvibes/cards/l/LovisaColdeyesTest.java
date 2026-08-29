package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LovisaColdeyesTest extends BaseCardTest {

    @Test
    @DisplayName("Barbarians, Warriors, and Berserkers get +2/+2 and haste")
    void buffsBarbariansWarriorsAndBerserkers() {
        harness.addToBattlefield(player1, new LovisaColdeyes());
        harness.addToBattlefield(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishBerserker());

        assertBuffed("Balduvian Barbarians", 5, 4);
        assertBuffed("Elvish Warrior", 4, 5);
        assertBuffed("Elvish Berserker", 3, 3);
    }

    @Test
    @DisplayName("Lovisa Coldeyes does not affect other creature types")
    void doesNotBuffOtherCreatureTypes() {
        harness.addToBattlefield(player1, new LovisaColdeyes());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The effect applies to matching creatures controlled by an opponent")
    void buffsOpponentMatchingCreatures() {
        harness.addToBattlefield(player1, new LovisaColdeyes());
        harness.addToBattlefield(player2, new ElvishWarrior());

        assertBuffed(player2, "Elvish Warrior", 4, 5);
    }

    private void assertBuffed(String name, int power, int toughness) {
        assertBuffed(player1, name, power, toughness);
    }

    private void assertBuffed(Player player, String name, int power, int toughness) {
        Permanent creature = findPermanent(player, name);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }
}
