package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoneymoonHearse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightwheelEnhancements.class, GrizzlyBears.class, HoneymoonHearse.class})
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

    @Test
    void canEnchantOpponentsCreatureWithoutStartingTheirEngines() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
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
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void opponentsMaxSpeedDoesNotPermitCastingAtSpeedThree() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LightwheelEnhancements()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
        harness.assertInGraveyard(player1, "Lightwheel Enhancements");
    }

    @Test
    void canCastAgainAfterEnchantedCreatureDiesAtMaxSpeed() {
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LightwheelEnhancements()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromGraveyardTargeting(player1, 0, firstBears.getId());
        harness.passBothPriorities();

        firstBears.setMarkedDamage(gqs.getEffectiveToughness(gd, firstBears));
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Lightwheel Enhancements");
        harness.assertNotOnBattlefield(player1, "Lightwheel Enhancements");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int auraIndex = gd.playerGraveyards.get(player1.getId()).indexOf(
                gd.playerGraveyards.get(player1.getId()).stream()
                        .filter(card -> card instanceof LightwheelEnhancements)
                        .findFirst().orElseThrow());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromGraveyardTargeting(player1, auraIndex, secondBears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightwheel Enhancements");
        harness.assertNotInGraveyard(player1, "Lightwheel Enhancements");
        assertThat(gqs.hasKeyword(gd, secondBears, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
    }
}
