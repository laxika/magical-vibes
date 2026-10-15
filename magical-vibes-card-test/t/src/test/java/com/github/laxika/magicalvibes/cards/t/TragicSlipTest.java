package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TragicSlip.class, GrizzlyBears.class, Shock.class, FountainOfYouth.class})
class TragicSlipTest extends BaseCardTest {

    

    @Test
    @DisplayName("Gives target creature -1/-1 without morbid")
    void givesMinusOneMinusOneWithoutMorbid() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -13/-13 with morbid")
    void givesMinusThirteenMinusThirteenWithMorbid() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Morbid is checked at resolution time")
    void morbidCheckedAtResolution() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Morbid triggers from any player's creature dying")
    void morbidTriggersFromAnyCreatureDeath() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Killing a creature with Shock enables morbid for Tragic Slip")
    void actualCreatureDeathEnablesMorbid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new Shock(), new TragicSlip()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("A creature dying in response enables morbid")
    void creatureDyingInResponseEnablesMorbid() {
        Permanent target = addCreature(player2);
        Permanent victim = addCreature(player1);
        harness.setHand(player1, List.of(new TragicSlip(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Morbid replaces the base penalty with exactly -13/-13 until end of turn")
    void morbidPenaltyIsExactlyThirteenAndExpires() {
        Permanent target = addCreature(player2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 14);
        Permanent victim = addCreature(player1);
        harness.setHand(player1, List.of(new Shock(), new TragicSlip()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-13);
        assertThat(target.getToughnessModifier()).isEqualTo(-13);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(16);
    }

    @Test
    @DisplayName("An illegal target at resolution leaves another creature unaffected")
    void targetDyingInResponseDoesNotRedirectSpell() {
        Permanent target = addCreature(player2);
        Permanent survivor = addCreature(player1);
        harness.setHand(player1, List.of(new TragicSlip(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tragic Slip");
        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
