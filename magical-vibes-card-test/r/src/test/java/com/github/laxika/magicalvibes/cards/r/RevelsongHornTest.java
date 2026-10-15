package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevelsongHorn.class, SafeholdElite.class})
class RevelsongHornTest extends BaseCardTest {

    @Test
    @DisplayName("A summoning-sick creature can pay the tap cost and be the target")
    void newlyEnteredCreatureCanPayCostAndReceiveBoost() {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new RevelsongHorn());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(horn.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped creature cannot pay the additional cost")
    void cannotUseTappedCreatureForCost() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        creature.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Horn cannot activate even with an untapped creature")
    void cannotActivateTappedHorn() {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new RevelsongHorn());
        horn.tap();
        Permanent creature = addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires one mana in addition to the tap costs")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        Permanent creature = addCreatureReady(player1, new SafeholdElite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Horn is not a legal target for its creature boost")
    void cannotTargetNoncreatureArtifact() {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new RevelsongHorn());
        addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, horn.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost resolves after the Horn and cost creature leave the battlefield")
    void resolvesIndependentlyOfSourceAndCostCreature() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        addCreatureReady(player1, new SafeholdElite());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving grants +1/+1 to the target creature")
    void resolvingGrantsBoost() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        addCreatureReady(player1, new SafeholdElite());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Safehold Elite 2/2 + 1/+1 = 3/3
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating taps the Horn and the creature paid as a cost")
    void tapsHornAndCostCreature() {
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new RevelsongHorn());
        Permanent costCreature = addCreatureReady(player1, new SafeholdElite());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(horn.isTapped()).isTrue();
        assertThat(costCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("+1/+1 boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        addCreatureReady(player1, new SafeholdElite());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature to tap for the cost")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RevelsongHorn());
        addCreatureReady(player1, new SafeholdElite());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
