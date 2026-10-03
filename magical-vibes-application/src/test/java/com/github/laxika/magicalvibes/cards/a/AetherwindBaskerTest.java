package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherwindBasker.class})
class AetherwindBaskerTest extends BaseCardTest {

    @Test
    void getsEnergyForEachCreatureOnEntering() {
        addCreatureReady(player1, new AetherwindBasker());
        harness.setHand(player1, List.of(new AetherwindBasker()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void getsEnergyForEachCreatureOnAttacking() {
        Permanent basker = addCreatureReady(player1, new AetherwindBasker());
        addCreatureReady(player1, new AetherwindBasker());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(basker.isTapped()).isTrue();
    }

    @Test
    void paysEnergyForPlusOnePlusOneUntilEndOfTurn() {
        Permanent basker = addCreatureReady(player1, new AetherwindBasker());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, basker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, basker)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, basker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, basker)).isEqualTo(7);
    }

    @Test
    void cannotActivateWithoutEnergy() {
        Permanent basker = addCreatureReady(player1, new AetherwindBasker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
        assertThat(gqs.getEffectivePower(gd, basker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, basker)).isEqualTo(7);
    }

    @Test
    void enteringAloneCountsItselfButNotOpposingCreatures() {
        addCreatureReady(player2, new AetherwindBasker());
        harness.setHand(player1, List.of(new AetherwindBasker()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void enterTriggerCountsCreaturesAtResolutionEvenAfterSourceLeaves() {
        Permanent other = addCreatureReady(player1, new AetherwindBasker());
        harness.setHand(player1, List.of(new AetherwindBasker()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(other.getId()))
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, entering));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void eachAttackingBaskerCountsAllControlledCreatures() {
        addCreatureReady(player1, new AetherwindBasker());
        addCreatureReady(player1, new AetherwindBasker());
        addCreatureReady(player2, new AetherwindBasker());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void repeatedActivationsPayImmediatelyAndBoostOnlyTheirSource() {
        Permanent basker = addCreatureReady(player1, new AetherwindBasker());
        Permanent other = addCreatureReady(player1, new AetherwindBasker());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, basker)).isEqualTo(7);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, basker)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, basker)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent basker = harness.addToBattlefieldAndReturn(player1, new AetherwindBasker());
        basker.tap();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, basker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, basker)).isEqualTo(8);
    }
}
