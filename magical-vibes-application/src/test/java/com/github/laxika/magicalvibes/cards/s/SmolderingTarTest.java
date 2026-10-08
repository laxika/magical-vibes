package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmolderingTar.class, AncientKavu.class})
class SmolderingTarTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger can make its controller lose 1 life")
    void upkeepTriggerTargetsController() {
        harness.addToBattlefield(player1, new SmolderingTar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Upkeep trigger can make the opponent lose 1 life")
    void upkeepTriggerTargetsOpponent() {
        harness.addToBattlefield(player1, new SmolderingTar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrificing Smoldering Tar deals 4 damage to target creature")
    void sacrificeAbilityDealsDamage() {
        harness.addToBattlefield(player1, new SmolderingTar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientKavu());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Smoldering Tar");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ancient Kavu");
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated outside sorcery timing")
    void sacrificeAbilityRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new SmolderingTar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientKavu());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated during an opponent's main phase")
    void sacrificeAbilityRequiresControllerTurn() {
        harness.addToBattlefield(player1, new SmolderingTar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientKavu());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Smoldering Tar does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SmolderingTar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated while another ability is on the stack")
    void sacrificeAbilityRequiresEmptyStack() {
        harness.addToBattlefield(player1, new SmolderingTar());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Smoldering Tar");
        harness.assertNotInGraveyard(player1, "Smoldering Tar");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a player and does not pay its cost for an invalid target")
    void sacrificeAbilityRejectsPlayerTarget() {
        harness.addToBattlefield(player1, new SmolderingTar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(RuntimeException.class);

        harness.assertOnBattlefield(player1, "Smoldering Tar");
        harness.assertNotInGraveyard(player1, "Smoldering Tar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice ability can target its controller's creature during the postcombat main phase")
    void sacrificeAbilityTargetsOwnCreatureAfterCombat() {
        harness.addToBattlefield(player1, new SmolderingTar());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Smoldering Tar");
        harness.assertOnBattlefield(player1, "Ancient Kavu");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ancient Kavu");
    }

    @Test
    @DisplayName("Sacrifice ability marks exactly 4 damage on a surviving creature")
    void sacrificeAbilityDealsExactlyFourDamage() {
        harness.addToBattlefield(player1, new SmolderingTar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ancient Kavu");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Smoldering Tar");
    }
}
