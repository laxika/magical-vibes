package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldDrillSergeant.class, KherKeep.class, GrizzlyBears.class})
class KoboldDrillSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobold creatures you control get +0/+1 and have trample")
    void buffsOtherKoboldsYouControlAndGrantsTrample() {
        Permanent kobold = createKoboldToken(player1);
        int basePower = gqs.getEffectivePower(gd, kobold);
        int baseToughness = gqs.getEffectiveToughness(gd, kobold);

        harness.addToBattlefield(player1, new KoboldDrillSergeant());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, kobold, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Kobold Drill Sergeant does not affect itself")
    void doesNotAffectItself() {
        harness.addToBattlefield(player1, new KoboldDrillSergeant());

        Permanent sergeant = findPermanent(player1, "Kobold Drill Sergeant");

        assertThat(gqs.getEffectivePower(gd, sergeant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sergeant)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect non-Kobold creatures")
    void doesNotAffectNonKobolds() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.addToBattlefield(player1, new KoboldDrillSergeant());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect an opponent's Kobolds")
    void doesNotAffectOpponentsKobolds() {
        Permanent opponentKobold = createKoboldToken(player2);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentKobold);

        harness.addToBattlefield(player1, new KoboldDrillSergeant());

        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, opponentKobold, Keyword.TRAMPLE)).isFalse();
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
