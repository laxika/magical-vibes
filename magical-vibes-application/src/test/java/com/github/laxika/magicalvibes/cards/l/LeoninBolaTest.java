package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeoninBolaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can unattach Leonin Bola to tap a target creature")
    void unattachAndTapTargetCreature() {
        Permanent equippedCreature = addReady(player1, new GrizzlyBears());
        Permanent bola = addBolaReady(player1);
        bola.setAttachedTo(equippedCreature.getId());
        Permanent target = addReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(equippedCreature.isTapped()).isTrue();
        assertThat(bola.getAttachedTo()).isNull();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Leonin Bola cannot target a land")
    void cannotTargetLand() {
        Permanent equippedCreature = addReady(player1, new GrizzlyBears());
        Permanent bola = addBolaReady(player1);
        bola.setAttachedTo(equippedCreature.getId());
        Permanent target = addReady(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addBolaReady(Player player) {
        return addReady(player, new LeoninBola());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
