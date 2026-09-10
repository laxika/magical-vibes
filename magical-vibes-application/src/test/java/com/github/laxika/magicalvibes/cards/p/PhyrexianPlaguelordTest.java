package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianPlaguelord.class, PhyrexianBroodlings.class, GrimMonolith.class})
class PhyrexianPlaguelordTest extends BaseCardTest {

    @Test
    @DisplayName("Tap/sacrifice ability gives target creature -4/-4 and sacrifices the Plaguelord")
    void tapSacAbilityGivesMinusFourMinusFour() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        PhyrexianBroodlings targetCard = new PhyrexianBroodlings();
        targetCard.setPower(7);
        targetCard.setToughness(7);
        Permanent target = addCreatureReady(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Plaguelord sacrificed as a cost
        harness.assertNotOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertInGraveyard(player1, "Phyrexian Plaguelord");

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Tap/sacrifice ability kills a 4-or-less toughness creature")
    void tapSacAbilityKillsSmallCreature() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        harness.addToBattlefield(player2, new PhyrexianBroodlings());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Phyrexian Broodlings"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Broodlings");
        harness.assertInGraveyard(player2, "Phyrexian Broodlings");
    }

    @Test
    @DisplayName("Tap/sacrifice ability cannot be activated while tapped")
    void tapSacAbilityCannotActivateWhenTapped() {
        Permanent plaguelord = addCreatureReady(player1, new PhyrexianPlaguelord());
        harness.addToBattlefield(player2, new PhyrexianBroodlings());
        plaguelord.tap();

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() ->
                        harness.activateAbility(player1, 0, null,
                                harness.getPermanentId(player2, "Phyrexian Broodlings")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("-4/-4 wears off at end of turn")
    void minusFourWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        PhyrexianBroodlings targetCard = new PhyrexianBroodlings();
        targetCard.setPower(8);
        targetCard.setToughness(8);
        Permanent target = addCreatureReady(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(8);
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability gives target creature -1/-1 and sacrifices the fodder")
    void sacCreatureAbilityGivesMinusOneMinusOne() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent fodder = addCreatureReady(player1, new PhyrexianBroodlings());

        PhyrexianBroodlings targetCard = new PhyrexianBroodlings();
        targetCard.setPower(3);
        targetCard.setToughness(3);
        Permanent target = addCreatureReady(player2, targetCard);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        // Fodder sacrificed, Plaguelord survives
        harness.assertInGraveyard(player1, "Phyrexian Broodlings");
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability does not require tapping the Plaguelord")
    void sacCreatureAbilityDoesNotTap() {
        Permanent plaguelord = addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent fodder = addCreatureReady(player1, new PhyrexianBroodlings());
        Permanent target = addCreatureReady(player2, new PhyrexianBroodlings());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(plaguelord.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability can sacrifice the Plaguelord itself")
    void sacCreatureAbilityCanSacrificeSource() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent target = addCreatureReady(player2, new PhyrexianBroodlings());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertInGraveyard(player1, "Phyrexian Plaguelord");
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both abilities require a creature target")
    void abilitiesCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertOnBattlefield(player2, "Grim Monolith");
    }

}
