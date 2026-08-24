package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SummaryJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a tapped creature during your main phase")
    void addendumDealsFiveDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Deals 3 damage to a tapped creature outside your main phase")
    void normalEffectDealsThreeDamage() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Serra Angel").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.tap();
        harness.setHand(player1, List.of(new SummaryJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Serra Angel").getMarkedDamage()).isZero();
    }
}
