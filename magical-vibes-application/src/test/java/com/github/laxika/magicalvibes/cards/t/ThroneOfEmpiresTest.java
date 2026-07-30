package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrownOfEmpires;
import com.github.laxika.magicalvibes.cards.s.ScepterOfEmpires;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThroneOfEmpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without both partners the ability creates one Soldier token")
    void createsOneTokenWithoutPartners() {
        addReadyArtifact(player1, new ThroneOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("With only one partner the ability still creates one Soldier token")
    void createsOneTokenWithOnePartner() {
        addReadyArtifact(player1, new ThroneOfEmpires());
        addReadyArtifact(player1, new CrownOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("With both partners the ability creates five Soldier tokens instead")
    void createsFiveTokensWithBothPartners() {
        addReadyArtifact(player1, new ThroneOfEmpires());
        addReadyArtifact(player1, new CrownOfEmpires());
        addReadyArtifact(player1, new ScepterOfEmpires());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
    }

    private Permanent addReadyArtifact(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
