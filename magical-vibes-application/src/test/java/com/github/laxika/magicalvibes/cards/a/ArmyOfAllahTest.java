package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmyOfAllah.class, GrizzlyBears.class})
class ArmyOfAllahTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all attacking creatures with +2/+0")
    void boostsAllAttackingCreatures() {
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArmyOfAllah()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(ownAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(opponentAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new ArmyOfAllah()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }
}
