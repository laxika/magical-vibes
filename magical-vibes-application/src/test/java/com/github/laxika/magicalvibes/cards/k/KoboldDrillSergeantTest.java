package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldDrillSergeant.class, KoboldTaskmaster.class, KasimirTheLoneWolf.class})
class KoboldDrillSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobold creatures you control get +0/+1 and have trample")
    void buffsOtherKoboldsYouControlAndGrantsTrample() {
        Permanent kobold = createKobold(player1);
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
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new KoboldDrillSergeant());
        Permanent opponentSergeant = harness.addToBattlefieldAndReturn(player2, new KoboldDrillSergeant());

        assertThat(gqs.getEffectivePower(gd, sergeant)).isEqualTo(gqs.getEffectivePower(gd, opponentSergeant));
        assertThat(gqs.getEffectiveToughness(gd, sergeant))
                .isEqualTo(gqs.getEffectiveToughness(gd, opponentSergeant));
        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.TRAMPLE))
                .isEqualTo(gqs.hasKeyword(gd, opponentSergeant, Keyword.TRAMPLE));
    }

    @Test
    @DisplayName("Does not affect non-Kobold creatures")
    void doesNotAffectNonKobolds() {
        Permanent nonKobold = harness.addToBattlefieldAndReturn(player1, new KasimirTheLoneWolf());
        int basePower = gqs.getEffectivePower(gd, nonKobold);
        int baseToughness = gqs.getEffectiveToughness(gd, nonKobold);
        boolean hadTrample = gqs.hasKeyword(gd, nonKobold, Keyword.TRAMPLE);

        harness.addToBattlefield(player1, new KoboldDrillSergeant());

        assertThat(gqs.getEffectivePower(gd, nonKobold)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, nonKobold)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, nonKobold, Keyword.TRAMPLE)).isEqualTo(hadTrample);
    }

    @Test
    @DisplayName("Does not affect an opponent's Kobolds")
    void doesNotAffectOpponentsKobolds() {
        Permanent opponentKobold = createKobold(player2);
        int basePower = gqs.getEffectivePower(gd, opponentKobold);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentKobold);
        boolean hadTrample = gqs.hasKeyword(gd, opponentKobold, Keyword.TRAMPLE);

        harness.addToBattlefield(player1, new KoboldDrillSergeant());

        assertThat(gqs.getEffectivePower(gd, opponentKobold)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, opponentKobold, Keyword.TRAMPLE)).isEqualTo(hadTrample);
    }

    @Test
    @DisplayName("Kobolds entering later also gain the bonus and trample")
    void affectsKoboldsEnteringLater() {
        harness.addToBattlefield(player1, new KoboldDrillSergeant());
        Permanent baseline = createKobold(player2);
        Permanent laterKobold = createKobold(player1);

        assertThat(gqs.getEffectivePower(gd, laterKobold)).isEqualTo(gqs.getEffectivePower(gd, baseline));
        assertThat(gqs.getEffectiveToughness(gd, laterKobold))
                .isEqualTo(gqs.getEffectiveToughness(gd, baseline) + 1);
        assertThat(gqs.hasKeyword(gd, laterKobold, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent createKobold(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new KoboldTaskmaster());
    }
}
