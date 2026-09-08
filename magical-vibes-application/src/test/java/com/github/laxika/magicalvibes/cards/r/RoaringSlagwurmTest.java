package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoaringSlagwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps every artifact on every battlefield")
    void attackingTapsAllArtifacts() {
        Permanent slagwurm = addCreatureReady(player1, new RoaringSlagwurm());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(slagwurm)));
        harness.passBothPriorities();

        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opponentArtifact.isTapped()).isTrue();
        assertThat(nonArtifact.isTapped()).isFalse();
    }
}
