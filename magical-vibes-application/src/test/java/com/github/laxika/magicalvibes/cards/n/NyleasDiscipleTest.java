package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Nylea's Disciple")
class NyleasDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to green devotion, including itself")
    void etbGainsLifeEqualToGreenDevotion() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.stack).isEmpty();
    }
}
