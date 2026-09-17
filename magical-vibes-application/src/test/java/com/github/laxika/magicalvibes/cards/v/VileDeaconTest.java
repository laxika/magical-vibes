package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.v.VexingBeetle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileDeacon.class, VexingBeetle.class})
class VileDeaconTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Cleric on the battlefield when it attacks")
    void getsPlusOneForEachClericOnBattlefield() {
        Permanent deacon = addCreatureReady(player1, new VileDeacon());
        addCreatureReady(player1, new VileDeacon());
        addCreatureReady(player2, new VileDeacon());

        int basePower = gqs.getEffectivePower(gd, deacon);
        int baseToughness = gqs.getEffectiveToughness(gd, deacon);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, deacon)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, deacon)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Does not count non-Clerics on the battlefield")
    void ignoresNonClerics() {
        Permanent deacon = addCreatureReady(player1, new VileDeacon());
        addCreatureReady(player2, new VexingBeetle());

        int basePower = gqs.getEffectivePower(gd, deacon);
        int baseToughness = gqs.getEffectiveToughness(gd, deacon);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, deacon)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, deacon)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Counts Clerics when the attack trigger resolves")
    void countsClericsAtResolution() {
        Permanent deacon = addCreatureReady(player1, new VileDeacon());
        int basePower = gqs.getEffectivePower(gd, deacon);
        int baseToughness = gqs.getEffectiveToughness(gd, deacon);

        declareAttackers(List.of(0));
        addCreatureReady(player2, new VileDeacon());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, deacon)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, deacon)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent deacon = addCreatureReady(player1, new VileDeacon());
        addCreatureReady(player2, new VileDeacon());

        int basePower = gqs.getEffectivePower(gd, deacon);
        int baseToughness = gqs.getEffectiveToughness(gd, deacon);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, deacon)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, deacon)).isEqualTo(baseToughness + 2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deacon)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, deacon)).isEqualTo(baseToughness);
    }

}
