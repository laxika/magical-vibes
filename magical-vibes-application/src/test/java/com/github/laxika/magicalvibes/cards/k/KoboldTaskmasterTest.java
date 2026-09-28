package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldTaskmaster.class, KoboldsOfKherKeep.class, BarbaryApes.class})
class KoboldTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobold creatures you control get +1/+0")
    void buffsOtherKoboldsYouControl() {
        Permanent kobold = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        int basePower = gqs.getEffectivePower(gd, kobold);

        harness.addToBattlefield(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kobold Taskmaster does not buff itself")
    void doesNotBuffItself() {
        Permanent taskmaster = harness.addToBattlefieldAndReturn(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, taskmaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taskmaster)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Kobold creatures")
    void doesNotBuffNonKobolds() {
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        int basePower = gqs.getEffectivePower(gd, apes);
        int baseToughness = gqs.getEffectiveToughness(gd, apes);

        harness.addToBattlefield(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, apes)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, apes)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff an opponent's Kobolds")
    void doesNotBuffOpponentKobolds() {
        Permanent opponentKobold = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());
        int basePower = gqs.getEffectivePower(gd, opponentKobold);

        harness.addToBattlefield(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, opponentKobold)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kobolds entering later also get +1/+0")
    void affectsKoboldsEnteringLater() {
        harness.addToBattlefield(player1, new KoboldTaskmaster());

        Permanent kobold = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(1);
    }
}
