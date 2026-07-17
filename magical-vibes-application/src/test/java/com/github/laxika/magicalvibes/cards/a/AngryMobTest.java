package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngryMobTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, P/T is 2 plus the Swamps your opponents control")
    void yourTurnAddsOpponentSwamps() {
        Permanent mob = addAngryMobReady(player1);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(4);
    }

    @Test
    @DisplayName("During your turn with no opponent Swamps, P/T is 2")
    void yourTurnNoOpponentSwamps() {
        Permanent mob = addAngryMobReady(player1);
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("During an opponent's turn, P/T is a flat 2 regardless of Swamps")
    void opponentTurnIsFlatTwo() {
        Permanent mob = addAngryMobReady(player1);
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only opponents' Swamps count, not your own")
    void ignoresControllersOwnSwamps() {
        Permanent mob = addAngryMobReady(player1);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(3);
    }

    private Permanent addAngryMobReady(Player player) {
        Permanent permanent = new Permanent(new AngryMob());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
