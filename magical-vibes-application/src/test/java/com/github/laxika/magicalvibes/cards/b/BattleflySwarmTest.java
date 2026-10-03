package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BattleflySwarm.class, PhyrexianRager.class})
class BattleflySwarmTest extends BaseCardTest {

    @Test
    @DisplayName("{B} grants Battlefly Swarm deathtouch until end of turn")
    void grantsDeathtouchUntilEndOfTurn() {
        Permanent swarm = addCreatureReady(player1, new BattleflySwarm());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, swarm, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, swarm, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch is granted only on resolution and only to the source")
    void grantsOnlySourceOnResolution() {
        Permanent swarm = addCreatureReady(player1, new BattleflySwarm());
        Permanent otherSwarm = addCreatureReady(player1, new BattleflySwarm());
        Permanent opposingSwarm = addCreatureReady(player2, new BattleflySwarm());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, swarm, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, swarm, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSwarm, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSwarm, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Battlefly Swarm can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent swarm = addCreatureReady(player1, new BattleflySwarm());
        swarm.setSummoningSick(true);
        swarm.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, swarm, Keyword.DEATHTOUCH)).isTrue();
        assertThat(swarm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the black activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent swarm = addCreatureReady(player1, new BattleflySwarm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, swarm, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Battlefly Swarm")
    void groundCreatureCannotBlockSwarm() {
        addCreatureReady(player1, new BattleflySwarm());
        addCreatureReady(player2, new PhyrexianRager());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Granted deathtouch kills a creature with more toughness than the Swarm's power")
    void deathtouchKillsLargerAttacker() {
        addCreatureReady(player1, new BattleflySwarm());
        addCreatureReady(player2, new PhyrexianRager());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player2, "Phyrexian Rager");
        harness.assertInGraveyard(player1, "Battlefly Swarm");
        harness.assertNotOnBattlefield(player2, "Phyrexian Rager");
    }
}
