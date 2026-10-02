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

@CardUsed({AkroanPhalanx.class, GrizzlyBears.class})
class AkroanPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{R} boosts creatures you control until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new AkroanPhalanx());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentsBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentsBears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
    }

    @Test
    void vigilanceKeepsAttackerUntapped() {
        Permanent phalanx = addCreatureReady(player1, new AkroanPhalanx());

        declareAttackers(List.of(0));

        assertThat(phalanx.isAttacking()).isTrue();
        assertThat(phalanx.isTapped()).isFalse();
    }

    @Test
    void tappedNewCreatureCanActivateRepeatedlyAndBoostsStack() {
        Permanent phalanx = harness.addToBattlefieldAndReturn(player1, new AkroanPhalanx());
        phalanx.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phalanx)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, phalanx)).isEqualTo(3);
        assertThat(phalanx.isTapped()).isTrue();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        Permanent phalanx = harness.addToBattlefieldAndReturn(player1, new AkroanPhalanx());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new AkroanPhalanx());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new AkroanPhalanx());

        assertThat(gqs.getEffectivePower(gd, phalanx)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
    }
}
