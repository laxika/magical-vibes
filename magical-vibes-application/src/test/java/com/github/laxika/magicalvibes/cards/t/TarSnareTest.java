package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AncientCrab;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarSnare.class, AirElemental.class, AncientCrab.class, GrizzlyBears.class, FountainOfYouth.class})
class TarSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-2 until end of turn")
    void givesTargetCreatureMinusThreeMinusTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castOn(target);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -2 toughness can kill a small creature")
    void killsSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOn(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castOn(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TarSnare()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Power can become negative without killing a creature with positive toughness")
    void negativePowerDoesNotKillCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientCrab());

        castOn(target);

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Ancient Crab");
        harness.assertInGraveyard(player1, "Tar Snare");
    }

    @Test
    @DisplayName("Reducing toughness can make previously marked damage lethal")
    void toughnessReductionMakesMarkedDamageLethal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientCrab());
        target.setMarkedDamage(3);

        castOn(target);

        harness.assertNotOnBattlefield(player2, "Ancient Crab");
        harness.assertInGraveyard(player2, "Ancient Crab");
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new TarSnare()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
