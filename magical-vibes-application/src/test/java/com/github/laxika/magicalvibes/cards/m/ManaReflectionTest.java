package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManaReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Without Mana Reflection a Forest taps for one green mana")
    void baselineSingleMana() {
        addForest(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("With Mana Reflection tapping a permanent for mana produces twice as much")
    void doublesProducedMana() {
        addForest(player1);
        addManaReflection(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Mana Reflections stack multiplicatively (quadruple)")
    void stacksMultiplicatively() {
        addForest(player1);
        addManaReflection(player1);
        addManaReflection(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's Mana Reflection does not double your mana")
    void onlyControllersManaDoubled() {
        addForest(player1);
        addManaReflection(player2);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addForest(Player player) {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(forest);
        return forest;
    }

    private Permanent addManaReflection(Player player) {
        Permanent reflection = new Permanent(new ManaReflection());
        gd.playerBattlefields.get(player.getId()).add(reflection);
        return reflection;
    }
}
