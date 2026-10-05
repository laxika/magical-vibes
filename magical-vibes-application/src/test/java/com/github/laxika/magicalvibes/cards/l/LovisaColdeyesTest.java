package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LovisaColdeyes.class, BalduvianBarbarians.class, ElvishBerserker.class,
        ElvishWarrior.class, GrizzlyBears.class, Boomerang.class})
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

    @Test
    @DisplayName("Matching creatures can attack immediately, but Lovisa cannot")
    void grantedHasteAllowsAttackDespiteSummoningSickness() {
        Permanent lovisa = harness.addToBattlefieldAndReturn(player1, new LovisaColdeyes());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        lovisa.setSummoningSick(true);
        warrior.setSummoningSick(true);

        assertThat(als.canAttack(gd, warrior, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, lovisa, player1.getId())).isFalse();
        assertThat(gqs.getEffectivePower(gd, lovisa)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lovisa)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lovisa, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The bonus and haste end when Lovisa leaves the battlefield")
    void bonusEndsWhenLovisaLeavesBattlefield() {
        Permanent lovisa = harness.addToBattlefieldAndReturn(player1, new LovisaColdeyes());
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        warrior.setSummoningSick(true);
        assertBuffed(player2, "Elvish Warrior", 4, 5);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, lovisa.getId());

        harness.assertNotOnBattlefield(player1, "Lovisa Coldeyes");
        harness.assertInHand(player1, "Lovisa Coldeyes");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, warrior, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Lovisas controlled by different players each grant their bonus")
    void bonusesFromOpposingLovisasStack() {
        harness.addToBattlefield(player1, new LovisaColdeyes());
        harness.addToBattlefield(player2, new LovisaColdeyes());
        harness.addToBattlefield(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player2, new ElvishWarrior());

        assertBuffed("Balduvian Barbarians", 7, 6);
        assertBuffed(player2, "Elvish Warrior", 6, 7);
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
