package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KraulHarpoonerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 from creature cards in the graveyard and may fight the chosen flier")
    void boostsAndFightsChosenFlyingCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Shock()));
        Permanent windDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        Permanent harpooner = castHarpooner();
        harness.handlePermanentChosen(player1, windDrake.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Wind Drake");
        assertThat(harpooner.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, harpooner)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the fight still applies the undergrowth boost")
    void decliningFightKeepsBoost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Shock()));
        Permanent windDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        Permanent harpooner = castHarpooner();
        harness.handlePermanentChosen(player1, windDrake.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, harpooner)).isEqualTo(5);
        assertThat(windDrake.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(windDrake);
    }

    @Test
    @DisplayName("Can target only a flying creature an opponent controls")
    void targetMustBeOpponentsFlyingCreature() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent windDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        castHarpooner();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, groundCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, windDrake.getId());
    }

    @Test
    @DisplayName("Gets its boost without a fight choice when no opposing flier exists")
    void noFlyingTargetStillGetsBoost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent harpooner = castHarpooner();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, harpooner)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castHarpooner() {
        harness.setHand(player1, List.of(new KraulHarpooner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Kraul Harpooner");
    }
}
