package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
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

@CardUsed({BrineHag.class, DoomBlade.class, GrizzlyBears.class})
class BrineHagTest extends BaseCardTest {

    @Test
    @DisplayName("Sets every creature that dealt damage to it this turn to base 0/2")
    void setsDamagingCreaturesToBaseZeroTwo() {
        Permanent damagingCreature = killBrineHagAfterCombatDamage();
        Permanent unaffectedCreature = addCreatureReady(player2, new GrizzlyBears());

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
        Permanent hag = addCreatureReady(player1, hagCard);

        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(1);
        Permanent damagingCreature = addCreatureReady(player2, bearCard);
        damagingCreature.setAttacking(true);
        hag.setBlocking(true);
        hag.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, hag.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return damagingCreature;
    }
}
