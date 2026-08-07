package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NineRingedBoTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a 1-toughness Spirit and exiles it instead of putting it into the graveyard")
    void killsAndExilesSpirit() {
        harness.addToBattlefield(player1, new NineRingedBo());
        harness.addToBattlefield(player2, new LanternKami());
        UUID targetId = harness.getPermanentId(player2, "Lantern Kami");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Lantern Kami");
        harness.assertNotInGraveyard(player2, "Lantern Kami");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Lantern Kami"));
    }

    @Test
    @DisplayName("Marks a surviving Spirit so a later death this turn exiles it")
    void marksSurvivingSpirit() {
        harness.addToBattlefield(player1, new NineRingedBo());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        UUID targetId = harness.getPermanentId(player2, "Kami of Old Stone");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(kami.getMarkedDamage()).isEqualTo(1);
        assertThat(kami.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpirit() {
        harness.addToBattlefield(player1, new NineRingedBo());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit");
    }
}
