package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianPlaguelord.class, BogImp.class, AladdinsRing.class})
class PhyrexianPlaguelordTest extends BaseCardTest {

    @Test
    @DisplayName("Tap/sacrifice ability gives target creature -4/-4 and sacrifices the Plaguelord")
    void tapSacAbilityGivesMinusFourMinusFour() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        BogImp targetCard = new BogImp();
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
        harness.addToBattlefield(player2, new BogImp());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Bog Imp"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bog Imp");
        harness.assertInGraveyard(player2, "Bog Imp");
    }

    @Test
    @DisplayName("Tap/sacrifice ability cannot be activated while tapped")
    void tapSacAbilityCannotActivateWhenTapped() {
        Permanent plaguelord = addCreatureReady(player1, new PhyrexianPlaguelord());
        harness.addToBattlefield(player2, new BogImp());
        plaguelord.tap();

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() ->
                        harness.activateAbility(player1, 0, null,
                                harness.getPermanentId(player2, "Bog Imp")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("-4/-4 wears off at end of turn")
    void minusFourWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        BogImp targetCard = new BogImp();
        targetCard.setPower(8);
        targetCard.setToughness(8);
        Permanent target = addCreatureReady(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(8);
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability gives target creature -1/-1 and sacrifices the fodder")
    void sacCreatureAbilityGivesMinusOneMinusOne() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent fodder = addCreatureReady(player1, new BogImp());

        BogImp targetCard = new BogImp();
        targetCard.setPower(3);
        targetCard.setToughness(3);
        Permanent target = addCreatureReady(player2, targetCard);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        // Fodder sacrificed, Plaguelord survives
        harness.assertInGraveyard(player1, "Bog Imp");
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
        Permanent fodder = addCreatureReady(player1, new BogImp());
        Permanent target = addCreatureReady(player2, new BogImp());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(plaguelord.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability can sacrifice the Plaguelord itself")
    void sacCreatureAbilityCanSacrificeSource() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        BogImp targetCard = new BogImp();
        targetCard.setPower(2);
        targetCard.setToughness(2);
        Permanent target = addCreatureReady(player2, targetCard);

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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AladdinsRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertOnBattlefield(player2, "Aladdin's Ring");
    }

    @Test
    @DisplayName("Tap/sacrifice ability cannot be activated with summoning sickness")
    void tapSacAbilityCannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new PhyrexianPlaguelord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogImp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertNotInGraveyard(player1, "Phyrexian Plaguelord");
    }

    @Test
    @DisplayName("Sacrifice-a-creature ability works while tapped and summoning sick")
    void sacCreatureAbilityWorksWhileTappedAndSummoningSick() {
        Permanent plaguelord = harness.addToBattlefieldAndReturn(player1, new PhyrexianPlaguelord());
        plaguelord.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogImp());

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Phyrexian Plaguelord");
        harness.assertOnBattlefield(player2, "Bog Imp");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bog Imp");
    }

    @Test
    @DisplayName("Tap/sacrifice ability pays its cost before resolution and can target itself")
    void tapSacAbilityCanTargetItself() {
        Permanent plaguelord = addCreatureReady(player1, new PhyrexianPlaguelord());

        harness.activateAbility(player1, 0, null, plaguelord.getId());

        harness.assertNotOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertInGraveyard(player1, "Phyrexian Plaguelord");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Phyrexian Plaguelord");
    }

    @Test
    @DisplayName("A targeted creature can also be sacrificed to pay the second ability's cost")
    void sacCreatureAbilityCanSacrificeItsTarget() {
        addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BogImp());

        harness.activateAbility(player1, 0, 1, null, fodder.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Bog Imp");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");
        harness.assertInGraveyard(player1, "Bog Imp");
    }

    @Test
    @DisplayName("The second ability can target a friendly creature and its -1/-1 expires")
    void sacCreatureAbilityCanTargetSourceAndWearsOff() {
        Permanent plaguelord = addCreatureReady(player1, new PhyrexianPlaguelord());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BogImp());

        harness.activateAbility(player1, 0, 1, null, plaguelord.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(plaguelord.getPowerModifier()).isEqualTo(-1);
        assertThat(plaguelord.getToughnessModifier()).isEqualTo(-1);
        harness.assertOnBattlefield(player1, "Phyrexian Plaguelord");

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(plaguelord.getPowerModifier()).isZero();
        assertThat(plaguelord.getToughnessModifier()).isZero();
    }

}
