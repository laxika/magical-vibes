package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CinderStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 7 damage to target player")
    void dealsSevenDamageToPlayer() {
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Deals 7 damage to a target creature, killing a 4/4")
    void killsFourToughnessCreature() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new CinderStorm()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }
}
