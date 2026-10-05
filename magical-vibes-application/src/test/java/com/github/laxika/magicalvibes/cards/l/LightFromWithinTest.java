package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BallynockTrapper;
import com.github.laxika.magicalvibes.cards.h.HearthfireHobgoblin;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.cards.s.SpiritOfTheHearth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightFromWithin.class, BallynockTrapper.class, HearthfireHobgoblin.class,
        SlipperyBogle.class, SpiritOfTheHearth.class})
class LightFromWithinTest extends BaseCardTest {

    @Test
    @DisplayName("One white mana symbol grants +1/+1")
    void oneWhiteSymbol() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent trapper = harness.addToBattlefieldAndReturn(player1, new BallynockTrapper()); // {3}{W}, 2/2
        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, trapper)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two white mana symbols grant +2/+2")
    void twoWhiteSymbols() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheHearth()); // {4}{W}{W}, 4/5
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(7);
    }

    @Test
    @DisplayName("Hybrid mana symbols containing white each count")
    void hybridSymbolsCount() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent hobgoblin = harness.addToBattlefieldAndReturn(player1, new HearthfireHobgoblin()); // {R/W}{R/W}{R/W}, 2/2
        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hobgoblin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creature with no white mana symbol gets no bonus")
    void noWhiteSymbol() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent bogle = harness.addToBattlefieldAndReturn(player1, new SlipperyBogle()); // {G/U}, 1/1
        assertThat(gqs.getEffectivePower(gd, bogle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bogle)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only affects creatures you control")
    void onlyOwnCreatures() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent opponentTrapper = harness.addToBattlefieldAndReturn(player2, new BallynockTrapper()); // opponent's {3}{W} creature
        assertThat(gqs.getEffectivePower(gd, opponentTrapper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentTrapper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Light from Within leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent trapper = harness.addToBattlefieldAndReturn(player1, new BallynockTrapper());
        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Light from Within"));

        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trapper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple copies each grant their full bonus")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new LightFromWithin());
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent hobgoblin = harness.addToBattlefieldAndReturn(player1, new HearthfireHobgoblin());

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, hobgoblin)).isEqualTo(8);
    }

    @Test
    @DisplayName("Resolving Light from Within boosts creatures already on the battlefield")
    void resolvingEnchantmentBoostsExistingCreatures() {
        Permanent trapper = harness.addToBattlefieldAndReturn(player1, new BallynockTrapper());

        harness.castFromHand(player1, new LightFromWithin(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trapper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, trapper)).isEqualTo(3);
    }

    @Test
    @DisplayName("A manifested creature has no mana cost and receives no bonus")
    void faceDownCreatureGetsNoBonus() {
        harness.addToBattlefield(player1, new LightFromWithin());
        Permanent hobgoblin = harness.addToBattlefieldAndReturn(player1, new HearthfireHobgoblin());
        hobgoblin.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(gqs.getEffectivePower(gd, hobgoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hobgoblin)).isEqualTo(2);
    }
}
