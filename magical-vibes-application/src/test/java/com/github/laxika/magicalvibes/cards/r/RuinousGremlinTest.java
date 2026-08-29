package com.github.laxika.magicalvibes.cards.r;

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

class RuinousGremlinTest extends BaseCardTest {

    @Test
    void sacrificesItselfToDestroyTargetArtifact() {
        Permanent gremlin = addReadyGremlin(player1);
        harness.addToBattlefield(player2, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Jalum Tome");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gremlin);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getOriginalCard() instanceof JalumTome);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof RuinousGremlin);
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        addReadyGremlin(player1);
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGremlin(Player player) {
        Permanent permanent = new Permanent(new RuinousGremlin());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
