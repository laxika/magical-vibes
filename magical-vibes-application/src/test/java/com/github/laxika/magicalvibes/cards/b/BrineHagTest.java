package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BrineHag.class)
class BrineHagTest extends BaseCardTest {

    @Test
    @DisplayName("Sets every creature that dealt damage to it this turn to base 0/2")
    void setsDamagingCreaturesToBaseZeroTwo() {
        Permanent unaffectedCreature = addCreatureReady(player1, new BrineHag());
        Permanent damagingCreature = killBrineHagAfterCombatDamage();

        assertThat(gqs.getEffectivePower(gd, damagingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, damagingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unaffectedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unaffectedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base-stat change lasts indefinitely")
    void lastsIndefinitely() {
        Permanent damagingCreature = killBrineHagAfterCombatDamage();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, damagingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, damagingCreature)).isEqualTo(2);
    }

    private Permanent killBrineHagAfterCombatDamage() {
        BrineHag hagCard = new BrineHag();
        hagCard.setPower(0);
        hagCard.setToughness(1);
        Permanent hag = addCreatureReady(player1, hagCard);

        Permanent damagingCreature = addCreatureReady(player2, new BrineHag());
        damagingCreature.setAttacking(true);
        hag.setBlocking(true);
        hag.addBlockingTarget(0);

        resolveCombat(player2);
        harness.passBothPriorities();
        return damagingCreature;
    }
}
