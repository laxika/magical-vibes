package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoneymoonHearse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LightwheelEnhancementsTest extends BaseCardTest {

    @Test
    void enchantsCreatureAndStartsEngines() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.setHand(player1, List.of(new LightwheelEnhancements()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void canEnchantVehicle() {
        Permanent hearse = harness.addToBattlefieldAndReturn(player1, new HoneymoonHearse());

        harness.setHand(player1, List.of(new LightwheelEnhancements()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, hearse.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hearse, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void graveyardCastRequiresMaxSpeed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LightwheelEnhancements()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");

        gd.playerSpeeds.put(player1.getId(), 4);
        harness.castFromGraveyardTargeting(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Lightwheel Enhancements");
    }
}
