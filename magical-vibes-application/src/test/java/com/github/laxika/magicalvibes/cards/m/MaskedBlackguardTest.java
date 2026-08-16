package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaskedBlackguardTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during the opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new MaskedBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability gives +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent blackguard = addReadyBlackguard(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isEqualTo(1);
        assertThat(blackguard.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations give a cumulative boost")
    void repeatedActivationsStack() {
        Permanent blackguard = addReadyBlackguard(player1);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isEqualTo(2);
        assertThat(blackguard.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent blackguard = addReadyBlackguard(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isZero();
        assertThat(blackguard.getToughnessModifier()).isZero();
    }

    private Permanent addReadyBlackguard(Player player) {
        Permanent perm = new Permanent(new MaskedBlackguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
