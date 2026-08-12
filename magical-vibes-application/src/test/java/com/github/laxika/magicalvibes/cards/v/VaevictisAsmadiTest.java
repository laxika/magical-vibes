package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaevictisAsmadiTest extends BaseCardTest {

    @Test
    @DisplayName("Each colored ability gives +1/+0 until end of turn")
    void coloredAbilitiesBoostSelf() {
        Permanent dragon = addDragon();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent dragon = addDragon();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(dragon.getPowerModifier()).isEqualTo(1);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Paying {B}{R}{G} at upkeep keeps Vaevictis Asmadi on the battlefield")
    void payingUpkeepKeepsDragon() {
        addDragon();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Vaevictis Asmadi");
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Vaevictis Asmadi")
    void decliningUpkeepSacrificesDragon() {
        addDragon();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Vaevictis Asmadi");
    }

    private Permanent addDragon() {
        return addCreatureReady(player1, new VaevictisAsmadi());
    }
}
