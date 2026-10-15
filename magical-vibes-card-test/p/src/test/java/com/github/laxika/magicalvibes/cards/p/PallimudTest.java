package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pallimud.class, Mountain.class})
class PallimudTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pallimud chooses the opponent as it enters without a triggered ability")
    void castingChoosesOpponentAsItEnters() {
        addOpponentLand(true);
        addOpponentLand(true);
        harness.setHand(player1, List.of(new Pallimud()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pallimud = findPermanent(player1, "Pallimud");
        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast still chooses the entering controller's opponent")
    void enteringWithoutCastingChoosesOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.tap();
        addOpponentLand(true);
        addOpponentLand(true);

        Permanent pallimud = harness.enterBattlefieldAndReturn(player2, new Pallimud());

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power decreases immediately when the chosen opponent's land untaps")
    void powerTracksLandsUntapping() {
        Permanent land = addOpponentLand(true);
        Permanent pallimud = harness.enterBattlefieldAndReturn(player1, new Pallimud());

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(1);
        land.untap();

        assertThat(gqs.getEffectivePower(gd, pallimud)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power equals the number of tapped lands the opponent controls; toughness stays 3")
    void powerCountsOpponentTappedLands() {
        Permanent pallimud = addPallimud();
        addOpponentLand(true);
        addOpponentLand(true);
        addOpponentLand(false);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
    }

    @Test
    @DisplayName("With no tapped opponent lands, power is 0")
    void noTappedLandsMeansZeroPower() {
        Permanent pallimud = addPallimud();
        addOpponentLand(false);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pallimud)).isEqualTo(3);
    }

    @Test
    @DisplayName("Your own tapped lands don't count")
    void ignoresControllersOwnTappedLands() {
        Permanent pallimud = addPallimud();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        ownLand.tap();
        addOpponentLand(true);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates when an opponent land becomes tapped")
    void powerTracksLandsTappingLater() {
        Permanent pallimud = addPallimud();
        Permanent land = addOpponentLand(false);

        assertThat(gqs.getEffectivePower(gd, pallimud)).isZero();

        land.tap();

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped nonland permanents don't count")
    void ignoresTappedNonlands() {
        Permanent pallimud = addPallimud();
        Permanent opponentCreature = addCreatureReady(player2, new Pallimud());
        opponentCreature.tap();

        assertThat(gqs.getEffectivePower(gd, pallimud)).isZero();
    }

    @Test
    @DisplayName("The chosen opponent remains fixed when Pallimud changes controller")
    void chosenOpponentDoesNotChangeWithControl() {
        Permanent pallimud = addPallimud();
        addOpponentLand(true);
        addOpponentLand(true);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        ownLand.tap();

        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(2);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), pallimud,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));

        assertThat(gd.findControllerOf(pallimud)).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, pallimud)).isEqualTo(2);
    }

    private Permanent addPallimud() {
        Permanent pallimud = addCreatureReady(player1, new Pallimud());
        pallimud.setRememberedTargetPlayerId(player2.getId());
        return pallimud;
    }

    private Permanent addOpponentLand(boolean tapped) {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        if (tapped) {
            land.tap();
        }
        return land;
    }
}
