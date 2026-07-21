package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfForgottenPharaohsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage when you control a Desert")
    void dealsDamageWithDesertOnBattlefield() {
        harness.setLife(player2, 20);
        Permanent wall = addReadyWall(player1);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability deals 1 damage when a Desert is in your graveyard")
    void dealsDamageWithDesertInGraveyard() {
        harness.setLife(player2, 20);
        Permanent wall = addReadyWall(player1);
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot activate without a Desert on battlefield or in graveyard")
    void cannotActivateWithoutDesert() {
        addReadyWall(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Desert");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        addReadyWall(player1);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfForgottenPharaohs());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
