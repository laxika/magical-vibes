package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Setessan Petitioner")
@CardUsed({SetessanPetitioner.class, GrizzlyBears.class, SternDismissal.class})
class SetessanPetitionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to green devotion, including itself")
    void etbGainsLifeEqualToGreenDevotionIncludingItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new SetessanPetitioner(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not count an opponent's green devotion")
    void doesNotCountOpponentsGreenDevotion() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new SetessanPetitioner(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Devotion is counted at resolution after another permanent leaves")
    void countsDevotionAtResolution() {
        harness.addToBattlefield(player1, new SetessanPetitioner());
        var supportId = harness.getPermanentId(player1, "Setessan Petitioner");
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new SetessanPetitioner(), "{1}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, supportId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger resolves with zero devotion after Petitioner leaves")
    void gainsNoLifeWhenSourceLeavesAndDevotionIsZero() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new SetessanPetitioner(), "{1}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Setessan Petitioner"));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Setessan Petitioner");
    }
}
