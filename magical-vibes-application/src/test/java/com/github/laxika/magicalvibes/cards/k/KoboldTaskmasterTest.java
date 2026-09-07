package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KherKeep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldTaskmaster.class, KherKeep.class, GrizzlyBears.class})
class KoboldTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobold creatures you control get +1/+0")
    void buffsOtherKoboldsYouControl() {
        Permanent kobold = createKoboldToken(player1);
        int basePower = gqs.getEffectivePower(gd, kobold);

        harness.addToBattlefield(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kobold Taskmaster does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new KoboldTaskmaster());

        Permanent taskmaster = findPermanent(player1, "Kobold Taskmaster");

        assertThat(gqs.getEffectivePower(gd, taskmaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taskmaster)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Kobold creatures")
    void doesNotBuffNonKobolds() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.addToBattlefield(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff an opponent's Kobolds")
    void doesNotBuffOpponentKobolds() {
        Permanent opponentKobold = createKoboldToken(player2);
        int basePower = gqs.getEffectivePower(gd, opponentKobold);

        harness.addToBattlefield(player1, new KoboldTaskmaster());

        assertThat(gqs.getEffectivePower(gd, opponentKobold)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(1);
    }

    private Permanent createKoboldToken(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new KherKeep());
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player, "Kobolds of Kher Keep");
    }
}
