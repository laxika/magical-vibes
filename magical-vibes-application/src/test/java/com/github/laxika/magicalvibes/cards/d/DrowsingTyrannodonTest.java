package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrowsingTyrannodonTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without a creature with power 4 or greater")
    void cannotAttackWithoutPowerFourCreature() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack while its controller controls a creature with power 4 or greater")
    void canAttackWithPowerFourCreature() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        addCreatureReady(player1, new CrawWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(tyrannodon.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature with power 4 or greater does not help")
    void opponentPowerFourCreatureDoesNotHelp() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        addCreatureReady(player2, new CrawWurm());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Stops attacking when the qualifying creature leaves the battlefield")
    void stopsAttackingWhenPowerFourCreatureLeaves() {
        Permanent tyrannodon = addCreatureReady(player1, new DrowsingTyrannodon());
        Permanent wurm = addCreatureReady(player1, new CrawWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).remove(wurm);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(tyrannodon.isAttacking()).isFalse();
    }
}
