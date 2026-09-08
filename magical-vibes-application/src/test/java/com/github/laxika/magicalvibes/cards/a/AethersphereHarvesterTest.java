package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent harvester = addHarvesterReady(player1);
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
        Permanent harvester = addHarvesterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(harvester), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, harvester)).isTrue();
        assertThat(gqs.getEffectivePower(gd, harvester)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, harvester)).isEqualTo(5);
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addHarvesterReady(Player player) {
        Permanent harvester = new Permanent(new AethersphereHarvester());
        harvester.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(harvester);
        return harvester;
    }
}
