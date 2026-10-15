package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.s.SneakAttack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildDogs.class, WornPowerstone.class, Rescind.class, SneakAttack.class})
class WildDogsTest extends BaseCardTest {

    private static final String WILD_DOGS = "Wild Dogs";

    @Test
    @DisplayName("Player with strictly the most life gains control during upkeep")
    void mostLifePlayerGainsControl() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, WILD_DOGS);
        harness.assertOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("Controller keeps Wild Dogs when they have the most life")
    void controllerKeepsWhenHighest() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 25);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("No control change when players are tied for the most life")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("Does not trigger when players are tied at the beginning of upkeep")
    void noTriggerWhenTieAtTriggerTime() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player2, 25);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("Does not change control when the upkeep condition fails at resolution")
    void noControlChangeWhenConditionFailsAtResolution() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("The current unique life leader gains control when the trigger resolves")
    void currentMostLifePlayerGainsControlAtResolution() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 25);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setLife(player1, 15);
        harness.setLife(player2, 30);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, WILD_DOGS);
        harness.assertOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("An old upkeep ability cannot change control of Wild Dogs after it leaves and returns")
    void oldTriggerDoesNotAffectReturnedCreature() {
        harness.addToBattlefield(player1, new SneakAttack());
        harness.addToBattlefield(player1, new WildDogs());
        var originalPermanentId = findPermanent(player1, WILD_DOGS).getId();
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Rescind()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, originalPermanentId);
        harness.passBothPriorities();
        harness.assertInHand(player1, WILD_DOGS);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, WILD_DOGS).getId()).isNotEqualTo(originalPermanentId);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("After control changes, the ability triggers during the new controller's upkeep")
    void triggersDuringNewControllersUpkeep() {
        harness.addToBattlefield(player1, new WildDogs());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, WILD_DOGS);

        harness.setLife(player1, 25);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, WILD_DOGS);
        harness.assertNotOnBattlefield(player2, WILD_DOGS);
    }

    @Test
    @DisplayName("Cycling cannot be activated with less than two mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new WildDogs()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, WILD_DOGS);
        harness.assertNotInGraveyard(player1, WILD_DOGS);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only when the ability resolves")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new WildDogs()));
        harness.setLibrary(player1, List.of(new WornPowerstone()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, WILD_DOGS);
        harness.assertInGraveyard(player1, WILD_DOGS);
        harness.assertNotInHand(player1, "Worn Powerstone");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Worn Powerstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards Wild Dogs and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new WildDogs()));
        harness.setLibrary(player1, List.of(new WornPowerstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, WILD_DOGS);
        harness.assertInHand(player1, "Worn Powerstone");
    }
}
