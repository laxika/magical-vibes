package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarFlareTest extends BaseCardTest {

    @Test
    void boostsAndUntapsOwnCreaturesOnly() {
        Permanent ownTappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownUntappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownTappedCreature.tap();
        opponentCreature.tap();

        castForMana();

        assertThat(ownTappedCreature.isTapped()).isFalse();
        assertThat(ownUntappedCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownTappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownTappedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownUntappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownUntappedCreature)).isEqualTo(3);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castForMana();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    private void castForMana() {
        harness.setHand(player1, List.of(new WarFlare()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
