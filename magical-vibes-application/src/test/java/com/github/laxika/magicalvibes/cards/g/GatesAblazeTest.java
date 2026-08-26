package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatesAblazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of Gates you control to each creature")
    void dealsDamageEqualToControlledGateCount() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(survivor.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent-controlled Gates do not increase the damage")
    void countsOnlyGatesYouControl() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals no damage when you control no Gates")
    void dealsNoDamageWithoutGates() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
    }

    private void cast() {
        harness.setHand(player1, List.of(new GatesAblaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
