package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnyaroBees.class, AshcoatBear.class})
class UnyaroBeesTest extends BaseCardTest {

    @Test
    @DisplayName("The green ability gives Unyaro Bees +1/+1 until end of turn")
    void boostsUntilEndOfTurn() {
        Permanent bees = addCreatureReady(player1, new UnyaroBees());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(1);
        assertThat(bees.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(0);
        assertThat(bees.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The sacrifice ability is paid as a cost and deals 2 damage to a player")
    void sacrificesAsCostAndDealsDamageToPlayer() {
        addCreatureReady(player1, new UnyaroBees());
        harness.setLife(player2, 20);
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Unyaro Bees");
        harness.assertInGraveyard(player1, "Unyaro Bees");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The sacrifice ability deals 2 damage to a creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new UnyaroBees());
        harness.addToBattlefield(player2, new AshcoatBear());
        addSacrificeAbilityMana();

        Permanent target = findPermanent(player2, "Ashcoat Bear");
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Repeated boosts work while summoning sick and tapped")
    void repeatedBoostsDoNotRequireTappingOrHaste() {
        Permanent bees = harness.addToBattlefieldAndReturn(player1, new UnyaroBees());
        bees.setSummoningSick(true);
        bees.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(bees.getEffectivePower()).isEqualTo(2);
        assertThat(bees.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The sacrifice ability works while summoning sick and tapped")
    void sacrificesWhileSummoningSickAndTapped() {
        Permanent bees = harness.addToBattlefieldAndReturn(player1, new UnyaroBees());
        bees.setSummoningSick(true);
        bees.tap();
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.assertInGraveyard(player1, "Unyaro Bees");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Sacrificing in response to a boost does not stop the damage ability")
    void sacrificeInResponseToBoost() {
        addCreatureReady(player1, new UnyaroBees());
        harness.addMana(player1, ManaColor.GREEN, 1);
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Unyaro Bees");
        assertThat(gd.stack).isEmpty();
    }

    private void addSacrificeAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
