package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
