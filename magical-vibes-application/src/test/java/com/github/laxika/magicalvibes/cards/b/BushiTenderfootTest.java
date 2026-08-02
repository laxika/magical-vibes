package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BushiTenderfootTest extends BaseCardTest {

    @Test
    @DisplayName("Flips after a creature dealt damage by it dies")
    void flipsAfterDamagedCreatureDies() {
        Permanent bushi = flipBushi();

        assertThat(bushi.isTransformed()).isTrue();
        assertThat(bushi.getCard().getName()).isEqualTo("Kenzo the Hardhearted");
        assertThat(gqs.getEffectivePower(gd, bushi)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bushi)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bushi, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kenzo gets +2/+2 when it becomes blocked")
    void kenzoGetsBushidoBonusWhenBlocked() {
        Permanent bushi = flipBushi();
        bushi.untap();
        bushi.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bushi)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bushi)).isEqualTo(6);
    }

    @Test
    @DisplayName("Kenzo gets +2/+2 when it blocks")
    void kenzoGetsBushidoBonusWhenItBlocks() {
        Permanent bushi = flipBushi();
        bushi.untap();
        bushi.setAttacking(false);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent attacker = findPermanent(player2, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bushi)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bushi)).isEqualTo(6);
    }

    private Permanent flipBushi() {
        harness.addToBattlefield(player1, new BushiTenderfoot());

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(0);
        blockerCard.setToughness(1);
        harness.addToBattlefield(player2, blockerCard);

        Permanent bushi = findPermanent(player1, "Bushi Tenderfoot");
        bushi.setSummoningSick(false);
        bushi.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        return bushi;
    }
}
