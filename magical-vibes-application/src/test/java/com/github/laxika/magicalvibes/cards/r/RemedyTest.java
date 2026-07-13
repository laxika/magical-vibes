package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemedyTest extends BaseCardTest {

    @Test
    void dividesPreventionAmongCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, Map.of(bears.getId(), 3, player2.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void appliesAllFivePreventionToSingleTarget() {
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(5);
    }

    @Test
    void preventionShieldWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    void assignmentsMustSumToFive() {
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }
}
