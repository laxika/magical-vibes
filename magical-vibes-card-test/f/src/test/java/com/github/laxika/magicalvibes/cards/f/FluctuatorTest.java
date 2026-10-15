package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.k.KarnSilverGolem;
import com.github.laxika.magicalvibes.cards.w.WildDogs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fluctuator.class, HazeOfPollen.class, Censor.class, WildDogs.class, Forest.class,
        Humble.class, KarnSilverGolem.class})
class FluctuatorTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces generic cycling costs by two")
    void reducesGenericCyclingCostsByTwo() {
        harness.addToBattlefield(player1, new Fluctuator());
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Haze of Pollen");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not remove colored mana requirements from cycling costs")
    void preservesColoredCyclingRequirements() {
        harness.addToBattlefield(player1, new Fluctuator());
        harness.setHand(player1, List.of(new Censor()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Censor");
    }

    @Test
    @DisplayName("Does not make cycling costs negative")
    void floorsGenericCyclingCostAtZero() {
        harness.addToBattlefield(player1, new Fluctuator());
        harness.setHand(player1, List.of(new WildDogs()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wild Dogs");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Only reduces cycling costs activated by its controller")
    void onlyReducesItsControllersCyclingCosts() {
        harness.addToBattlefield(player2, new Fluctuator());
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Haze of Pollen");
    }

    @Test
    @DisplayName("A cycling cost of three still requires one mana with one Fluctuator")
    void doesNotApplyItsReductionTwice() {
        harness.addToBattlefield(player1, new Fluctuator());
        harness.setHand(player1, List.of(new HazeOfPollen()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Haze of Pollen");
        harness.assertNotInGraveyard(player1, "Haze of Pollen");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated Fluctuator stops reducing costs when it loses all abilities")
    void doesNotReduceCostsAfterLosingItsAbilities() {
        var fluctuator = harness.addToBattlefieldAndReturn(player1, new Fluctuator());
        harness.addToBattlefield(player1, new KarnSilverGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, fluctuator.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, fluctuator.getId());
        harness.assertOnBattlefield(player1, "Fluctuator");

        harness.setHand(player1, List.of(new WildDogs()));
        harness.setLibrary(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Wild Dogs");
        assertThat(gd.stack).isEmpty();
    }
}
