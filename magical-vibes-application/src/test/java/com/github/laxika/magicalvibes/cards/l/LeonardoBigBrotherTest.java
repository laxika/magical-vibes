package com.github.laxika.magicalvibes.cards.l;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardoBigBrother.class, GrizzlyBears.class})
class LeonardoBigBrotherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other creature its controller controls")
    void getsPowerForOtherCreaturesYouControl() {
        Permanent leonardo = harness.addToBattlefieldAndReturn(player1, new LeonardoBigBrother());
        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(1);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leonardo)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Leonardo in tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoBigBrother()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent leonardo = findPermanent(player1, "Leonardo, Big Brother");
        assertThat(leonardo.isTapped()).isTrue();
        assertThat(leonardo.isAttacking()).isTrue();
        assertThat(leonardo.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Sneak is only available during its controller's declare blockers step")
    void sneakIsRestrictedToYourDeclareBlockersStep() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoBigBrother()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
