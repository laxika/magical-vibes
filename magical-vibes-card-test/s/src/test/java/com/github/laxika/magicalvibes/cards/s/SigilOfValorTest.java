package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MaritimeGuard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigilOfValor.class, MaritimeGuard.class})
class SigilOfValorTest extends BaseCardTest {

    @Test
    void anotherCreatureAttackingAloneDoesNotTriggerSigil() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new MaritimeGuard());

        declareAttackers(List.of(2));

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void equipCostsOneManaAndAttachesToOwnCreature() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfValor());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sigil.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new MaritimeGuard());
        harness.addToBattlefield(player1, new SigilOfValor());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        harness.addToBattlefield(player1, new SigilOfValor());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsCreaturesAtResolutionAndBonusRemainsFixed() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player2, new MaritimeGuard());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent other = addCreatureReady(player1, new MaritimeGuard());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).remove(other);
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void triggerStillBoostsAttackerAfterSigilBecomesUnattached() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new MaritimeGuard());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        sigil.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void triggerBoostsOriginalAttackerAfterAttachmentChanges() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        Permanent other = addCreatureReady(player1, new MaritimeGuard());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        sigil.setAttachedTo(other.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    void triggerStillResolvesAfterSigilLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = harness.addToBattlefieldAndReturn(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new MaritimeGuard());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sigil);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking alone boosts the equipped creature per other creature you control")
    void attacksAloneBoostsPerOtherCreature() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new MaritimeGuard());
        addCreatureReady(player1, new MaritimeGuard());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone with no other creatures gives no boost")
    void attacksAloneWithNoOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("No trigger when the equipped creature doesn't attack alone")
    void noTriggerWhenNotAttackingAlone() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new MaritimeGuard());

        // Index 1 is the Equipment, so the second creature is index 2.
        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new MaritimeGuard());
        Permanent sigil = addCreatureReady(player1, new SigilOfValor());
        sigil.setAttachedTo(creature.getId());
        addCreatureReady(player1, new MaritimeGuard());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }
}
