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
@CardUsed({SetessanPetitioner.class, GrizzlyBears.class})
class SetessanPetitionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to green devotion, including itself")
    void etbGainsLifeEqualToGreenDevotionIncludingItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SetessanPetitioner()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not count an opponent's green devotion")
    void doesNotCountOpponentsGreenDevotion() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SetessanPetitioner()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
