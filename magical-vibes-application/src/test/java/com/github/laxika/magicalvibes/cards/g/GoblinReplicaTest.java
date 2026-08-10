package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinReplicaTest extends BaseCardTest {

    @Test
    void sacrificesItselfToDestroyTargetArtifact() {
        Permanent replica = addReadyReplica(player1);
        harness.addToBattlefield(player2, new JalumTome());
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Jalum Tome");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(replica);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getOriginalCard() instanceof JalumTome);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GoblinReplica);
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        addReadyReplica(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyReplica(Player player) {
        Permanent permanent = new Permanent(new GoblinReplica());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
