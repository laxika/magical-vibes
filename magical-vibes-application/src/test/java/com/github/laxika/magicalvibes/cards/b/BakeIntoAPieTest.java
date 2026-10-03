package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BakeIntoAPie.class, LlanowarElves.class, Forest.class})
class BakeIntoAPieTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted creature and creates a Food token")
    void destroysCreatureAndCreatesFood() {
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID target = harness.getPermanentId(player2, "Llanowar Elves");

        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target);

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Food created by Bake into a Pie can be sacrificed for life")
    void createdFoodCanBeSacrificedForLife() {
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID target = harness.getPermanentId(player2, "Llanowar Elves");

        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID target = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own creature and create Food")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID target = harness.getPermanentId(player1, "Llanowar Elves");
        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Food");
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Creates no Food if the only target leaves before resolution")
    void createsNoFoodWhenTargetLeaves() {
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID target = harness.getPermanentId(player2, "Llanowar Elves");
        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.setHand(player2, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, List.of(target));

        harness.castAndResolveInstant(player2, 0, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Bake into a Pie");
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("A tapped Food cannot pay the tap cost of its ability")
    void tappedFoodCannotBeActivated() {
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID target = harness.getPermanentId(player2, "Llanowar Elves");
        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, target);
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Food requires two mana to activate")
    void foodRequiresTwoMana() {
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID target = harness.getPermanentId(player2, "Llanowar Elves");
        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, target);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }
}
