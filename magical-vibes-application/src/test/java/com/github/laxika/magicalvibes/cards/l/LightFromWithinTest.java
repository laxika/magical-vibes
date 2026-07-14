package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KitchenFinks;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LightFromWithinTest extends BaseCardTest {

    private Permanent creature(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("One white mana symbol grants +1/+1")
    void oneWhiteSymbol() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player1, new SavannahLions()); // {W}, 2/1

        Permanent lions = creature(player1, "Savannah Lions");
        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two white mana symbols grant +2/+2")
    void twoWhiteSymbols() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player1, new WhiteKnight()); // {W}{W}, 2/2

        Permanent knight = creature(player1, "White Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Hybrid mana symbols containing white each count")
    void hybridSymbolsCount() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player1, new KitchenFinks()); // {1}{G/W}{G/W}, 3/2

        Permanent finks = creature(player1, "Kitchen Finks");
        assertThat(gqs.getEffectivePower(gd, finks)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, finks)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature with no white mana symbol gets no bonus")
    void noWhiteSymbol() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, 2/2

        Permanent bears = creature(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only affects creatures you control")
    void onlyOwnCreatures() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player2, new SavannahLions()); // opponent's {W} creature

        Permanent opponentLions = creature(player2, "Savannah Lions");
        assertThat(gqs.getEffectivePower(gd, opponentLions)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentLions)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus is removed when Light from Within leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player1, new SavannahLions());

        Permanent lions = creature(player1, "Savannah Lions");
        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Light from Within"));

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(1);
    }
}
