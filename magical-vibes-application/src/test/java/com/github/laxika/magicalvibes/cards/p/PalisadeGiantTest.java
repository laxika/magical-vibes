package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PalisadeGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Damage that would be dealt to you is dealt to the Giant instead")
    void damageToControllerRedirectedToGiant() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Palisade Giant");
    }

    @Test
    @DisplayName("Damage that would be dealt to another permanent you control is dealt to the Giant instead")
    void damageToOtherPermanentRedirectedToGiant() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent bears = gd.playerBattlefields.get(player2.getId()).get(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(0);
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage dealt to the Giant itself is not redirected")
    void damageToGiantIsNotRedirected() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Palisade Giant");
    }

    @Test
    @DisplayName("An opponent's permanents are unaffected by the redirect")
    void opponentPermanentsAreUnaffected() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(giant.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Enough redirected damage destroys the Giant, sparing its controller")
    void lethalRedirectedDamageDestroysGiant() {
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 11);

        // 7 damage aimed at the controller is redirected to the 2/7 Giant, destroying it.
        harness.castInstant(player1, 0, 7, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Palisade Giant");
    }
}
