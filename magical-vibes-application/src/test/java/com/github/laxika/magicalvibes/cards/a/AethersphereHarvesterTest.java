package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethersphereHarvester.class, AetherSwooper.class})
class AethersphereHarvesterTest extends BaseCardTest {

    @Test
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new AethersphereHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysEnergyForLifelinkUntilEndOfTurn() {
        Permanent harvester = addCreatureReady(player1, new AethersphereHarvester());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(harvester), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, harvester, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, harvester, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void crewsWithOnePower() {
        Permanent harvester = addCreatureReady(player1, new AethersphereHarvester());
        Permanent creature = addCreatureReady(player1, new AetherSwooper());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(harvester), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, harvester)).isTrue();
        assertThat(gqs.getEffectivePower(gd, harvester)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, harvester)).isEqualTo(5);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void energyIsPaidBeforeLifelinkResolves() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AethersphereHarvester());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, harvester, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, harvester, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void cannotGainLifelinkWithoutEnergy() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AethersphereHarvester());
        gd.playerEnergyCounters.put(player1.getId(), 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, harvester, Keyword.LIFELINK)).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void lifelinkGainedBeforeCrewingGainsLifeFromCombatDamage() {
        Permanent harvester = addCreatureReady(player1, new AethersphereHarvester());
        harness.addToBattlefield(player1, new AetherSwooper());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, harvester)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationEndsAtCleanup() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AethersphereHarvester());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AetherSwooper());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, harvester)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, harvester)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, harvester)).isFalse();
    }
}
