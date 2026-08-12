package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelJiladWolfTest extends BaseCardTest {

    @Test
    @DisplayName("When Tel-Jilad Wolf becomes blocked by an artifact creature, it gets +3/+3")
    void becomesBlockedByArtifactCreatureBoosts() {
        Permanent wolf = addReady(player1, new TelJiladWolf());
        wolf.setAttacking(true);
        addReady(player2, new Ornithopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(3);
        assertThat(wolf.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("When Tel-Jilad Wolf becomes blocked by a nonartifact creature, it gets no boost")
    void becomesBlockedByNonartifactCreatureDoesNothing() {
        Permanent wolf = addReady(player1, new TelJiladWolf());
        wolf.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking an artifact creature does not trigger Tel-Jilad Wolf")
    void blockingArtifactCreatureDoesNothing() {
        Permanent artifactCreature = addReady(player1, new CopperMyr());
        artifactCreature.setAttacking(true);
        Permanent wolf = addReady(player2, new TelJiladWolf());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
