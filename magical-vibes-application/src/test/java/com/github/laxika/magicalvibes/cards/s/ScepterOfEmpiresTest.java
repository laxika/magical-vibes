package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrownOfEmpires;
import com.github.laxika.magicalvibes.cards.t.ThroneOfEmpires;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScepterOfEmpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without both partners the ability deals 1 damage")
    void dealsOneDamageWithoutPartners() {
        addReadyArtifact(player1, new ScepterOfEmpires());
        int startingLife = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With only one partner the ability still deals 1 damage")
    void dealsOneDamageWithOnePartner() {
        addReadyArtifact(player1, new ScepterOfEmpires());
        addReadyArtifact(player1, new CrownOfEmpires());
        int startingLife = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With both partners the ability deals 3 damage instead")
    void dealsThreeDamageWithBothPartners() {
        addReadyArtifact(player1, new ScepterOfEmpires());
        addReadyArtifact(player1, new CrownOfEmpires());
        addReadyArtifact(player1, new ThroneOfEmpires());
        int startingLife = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 3);
    }

    private Permanent addReadyArtifact(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
