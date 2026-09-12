package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CityInABottle.class, FlyingMen.class, GrizzlyBears.class, Mountain.class})
class CityInABottleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices all other nontoken permanents originally printed in Arabian Nights")
    void sacrificesOtherArabianNightsPermanents() {
        harness.addToBattlefield(player1, new CityInABottle());
        harness.addToBattlefield(player1, new FlyingMen());
        harness.addToBattlefield(player2, new FlyingMen());

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "City in a Bottle");
        harness.assertNotOnBattlefield(player1, "Flying Men");
        harness.assertNotOnBattlefield(player2, "Flying Men");
        harness.assertInGraveyard(player1, "Flying Men");
        harness.assertInGraveyard(player2, "Flying Men");
    }

    @Test
    @DisplayName("Does not sacrifice itself")
    void doesNotSacrificeItself() {
        harness.addToBattlefield(player1, new CityInABottle());

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "City in a Bottle");
    }

    @Test
    @DisplayName("Prevents casting Arabian Nights spells and playing Arabian Nights lands")
    void preventsArabianNightsSpellsAndLands() {
        harness.addToBattlefield(player1, new CityInABottle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FlyingMen()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player2, List.of(new Mountain()));
        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Allows spells whose names were not originally printed in Arabian Nights")
    void allowsNonArabianNightsSpells() {
        harness.addToBattlefield(player1, new CityInABottle());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
