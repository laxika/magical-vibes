package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OmnathLocusOfManaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each unspent green mana its controller has")
    void getsBoostFromUnspentGreenMana() {
        Permanent omnath = addCreatureReady(player1, new OmnathLocusOfMana());
        int basePower = gqs.getEffectivePower(gd, omnath);
        int baseToughness = gqs.getEffectiveToughness(gd, omnath);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, omnath)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Preserves only its controller's green mana across a step boundary")
    void preservesOnlyControllersGreenMana() {
        Permanent omnath = addCreatureReady(player1, new OmnathLocusOfMana());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.GREEN, 4);

        advanceToUpkeep(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(4);
    }
}
