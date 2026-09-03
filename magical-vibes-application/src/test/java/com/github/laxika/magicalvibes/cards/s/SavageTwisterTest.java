package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.t.TalruumMinotaur;
import com.github.laxika.magicalvibes.cards.v.VolcanicDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavageTwister.class, FeralShadow.class, TalruumMinotaur.class, VolcanicDragon.class})
class SavageTwisterTest extends BaseCardTest {

    private void castTwister(int xValue) {
        harness.setHand(player1, List.of(new SavageTwister()));
        harness.addMana(player1, ManaColor.RED, 1 + xValue);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Savage Twister deals X damage to each creature, killing those with toughness <= X")
    void dealsXDamageToEachCreature() {
        harness.addToBattlefield(player1, new FeralShadow());
        harness.addToBattlefield(player2, new TalruumMinotaur());

        castTwister(3);

        harness.assertNotOnBattlefield(player1, "Feral Shadow");
        harness.assertNotOnBattlefield(player2, "Talruum Minotaur");
    }

    @Test
    @DisplayName("Savage Twister damages flying creatures too")
    void damagesFliers() {
        harness.addToBattlefield(player2, new VolcanicDragon());

        castTwister(4);

        harness.assertNotOnBattlefield(player2, "Volcanic Dragon");
    }

    @Test
    @DisplayName("Creatures with toughness greater than X survive")
    void toughCreaturesSurvive() {
        harness.addToBattlefield(player2, new TalruumMinotaur());

        castTwister(2);

        harness.assertOnBattlefield(player2, "Talruum Minotaur");
    }

    @Test
    @DisplayName("Savage Twister deals no damage to players")
    void dealsNoDamageToPlayers() {
        castTwister(3);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Savage Twister with X=0 kills nothing")
    void xZeroKillsNothing() {
        harness.addToBattlefield(player2, new FeralShadow());

        castTwister(0);

        harness.assertOnBattlefield(player2, "Feral Shadow");
    }
}
