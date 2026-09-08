package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemolitionStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 5 animates Demolition Stomper and taps creatures with total power 5")
    void crewAnimatesStomper() {
        Permanent stomper = addStomperReady(player1);
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(stomper.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, stomper)).isTrue();
        assertThat(giant.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Demolition Stomper cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        addAttackingStomper();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Demolition Stomper can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        addAttackingStomper();
        Permanent giant = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    private Permanent addStomperReady(Player player) {
        return addCreatureReady(player, new DemolitionStomper());
    }

    private Permanent addAttackingStomper() {
        Permanent stomper = new Permanent(new DemolitionStomper());
        stomper.setSummoningSick(false);
        stomper.setAnimatedUntilEndOfTurn(true);
        stomper.setAnimatedPower(10);
        stomper.setAnimatedToughness(7);
        stomper.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(stomper);
        return stomper;
    }
}
