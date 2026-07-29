package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelimTorsDartsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target player")
    void dealsOneDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability taps the artifact, so it cannot be activated twice")
    void abilityTapsTheArtifact() {
        harness.setLife(player2, 20);
        Permanent darts = addReadyDarts(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(darts.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyDarts(Player player) {
        Permanent perm = new Permanent(new TelimTorsDarts());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
