package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurVeteran.class})
class CentaurVeteranTest extends BaseCardTest {

    @Test
    void payingGreenAndDiscardingACardGrantsARegenerationShield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new CentaurVeteran());
        harness.setHand(player1, List.of(new CentaurVeteran()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(veteran.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Centaur Veteran");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new CentaurVeteran());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new CentaurVeteran());
        harness.setHand(player1, List.of(new CentaurVeteran()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void regenerationShieldPreventsLethalCombatDamage() {
        Permanent veteran = addCreatureReady(player1, new CentaurVeteran());
        harness.setHand(player1, List.of(new CentaurVeteran()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new CentaurVeteran());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(veteran.getId(), 3));

        harness.assertOnBattlefield(player1, "Centaur Veteran");
        assertThat(veteran.isTapped()).isTrue();
        assertThat(veteran.getRegenerationShield()).isZero();
        assertThat(veteran.getMarkedDamage()).isZero();
    }
}
