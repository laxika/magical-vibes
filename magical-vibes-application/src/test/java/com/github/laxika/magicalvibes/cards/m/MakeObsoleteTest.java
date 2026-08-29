package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MakeObsoleteTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures opponents control -1/-1 and leaves your own creatures alone")
    void weakensOnlyOpponentCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enemyBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMakeObsolete();

        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(enemyBear.getEffectivePower()).isEqualTo(1);
        assertThat(enemyBear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills an opponent's 1/1")
    void killsOneToughnessOpponentCreature() {
        harness.addToBattlefield(player2, new FugitiveWizard());

        castMakeObsolete();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent enemyBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMakeObsolete();

        assertThat(enemyBear.getEffectivePower()).isEqualTo(1);
        assertThat(enemyBear.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enemyBear.getEffectivePower()).isEqualTo(2);
        assertThat(enemyBear.getEffectiveToughness()).isEqualTo(2);
    }

    private void castMakeObsolete() {
        harness.setHand(player1, List.of(new MakeObsolete()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
