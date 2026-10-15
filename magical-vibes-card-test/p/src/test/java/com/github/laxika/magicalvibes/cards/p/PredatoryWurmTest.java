package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GarrukUnleashed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatoryWurm.class, GarrukUnleashed.class})
class PredatoryWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 while its controller controls a planeswalker")
    void getsBoostWithPlaneswalker() {
        Permanent wurm = addCreatureReady(player1, new PredatoryWurm());
        int basePower = gqs.getEffectivePower(gd, wurm);
        int baseToughness = gqs.getEffectiveToughness(gd, wurm);

        harness.addToBattlefield(player1, new GarrukUnleashed());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Does not get the boost without a planeswalker")
    void noBoostWithoutPlaneswalker() {
        Permanent wurm = addCreatureReady(player1, new PredatoryWurm());
        int basePower = gqs.getEffectivePower(gd, wurm);
        int baseToughness = gqs.getEffectiveToughness(gd, wurm);

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("An opponent's planeswalker does not grant the boost")
    void opponentPlaneswalkerDoesNotCount() {
        Permanent wurm = addCreatureReady(player1, new PredatoryWurm());
        int basePower = gqs.getEffectivePower(gd, wurm);
        int baseToughness = gqs.getEffectiveToughness(gd, wurm);

        harness.addToBattlefield(player2, new GarrukUnleashed());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Loses the boost when the planeswalker leaves")
    void losesBoostWhenPlaneswalkerLeaves() {
        Permanent wurm = addCreatureReady(player1, new PredatoryWurm());
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukUnleashed());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(garruk);

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(4);
    }
}
