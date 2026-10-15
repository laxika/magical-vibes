package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CylianSunsinger.class, GrizzlyBears.class, Unsummon.class})
class CylianSunsingerTest extends BaseCardTest {

    private void addRgwMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Ability gives +3/+3 to itself and every creature with the same name, on any side")
    void boostsSelfAndAllSameNameCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent ownCopy = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppCopy = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        harness.forceActivePlayer(player1);
        addRgwMana(player1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        // Source and every other same-name creature (both controllers) get +3/+3
        assertThat(source.getEffectivePower()).isEqualTo(5);
        assertThat(source.getEffectiveToughness()).isEqualTo(5);
        assertThat(ownCopy.getEffectivePower()).isEqualTo(5);
        assertThat(ownCopy.getEffectiveToughness()).isEqualTo(5);
        assertThat(oppCopy.getEffectivePower()).isEqualTo(5);
        assertThat(oppCopy.getEffectiveToughness()).isEqualTo(5);

        // Different-named creature is untouched
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +3/+3 boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());

        harness.forceActivePlayer(player1);
        addRgwMana(player1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(0);
        assertThat(source.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void boostsSameNameCreaturesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent ownCopy = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent opponentCopy = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.forceActivePlayer(player1);
        addRgwMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInHand(player1, "Cylian Sunsinger");
        harness.passBothPriorities();

        assertThat(ownCopy.getEffectivePower()).isEqualTo(5);
        assertThat(ownCopy.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponentCopy.getEffectivePower()).isEqualTo(5);
        assertThat(opponentCopy.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void onlyCreaturesPresentAtResolutionReceiveBoost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        harness.forceActivePlayer(player1);
        addRgwMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        assertThat(source.getEffectivePower()).isEqualTo(5);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(5);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(5);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void repeatedActivationsStackWithoutTappingSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        harness.forceActivePlayer(player1);
        addRgwMana(player1);
        addRgwMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(8);
        assertThat(source.getEffectiveToughness()).isEqualTo(8);
        assertThat(source.isTapped()).isFalse();
    }
}
