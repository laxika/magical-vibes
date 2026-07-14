package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChaoticBacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the number of white and/or blue permanents the target controls")
    void dealsTwiceWhiteAndBluePermanents() {
        // Target controls 1 white and 2 blue permanents = 3, doubled = 6 damage.
        addCreatureReady(player2, new SuntailHawk());
        addCreatureReady(player2, new FugitiveWizard());
        addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14); // 20 - 6
    }

    @Test
    @DisplayName("Does not count permanents that are neither white nor blue")
    void ignoresOtherColors() {
        addCreatureReady(player2, new GrizzlyBears()); // green
        addCreatureReady(player2, new HillGiant());    // red
        addCreatureReady(player2, new SuntailHawk());  // white

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only the white permanent counts = 1, doubled = 2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // 20 - 2
    }

    @Test
    @DisplayName("Deals no damage when target controls no white or blue permanents")
    void dealsNoDamageWithNoWhiteOrBlue() {
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
