package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ScourgeOfTheSkyclaves.class)
class ScourgeOfTheSkyclavesTest extends BaseCardTest {

    @Test
    @DisplayName("A kicked cast makes each player lose half their life, rounded up")
    void kickedCastMakesEachPlayerLoseHalfLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 9);
        harness.setHand(player1, List.of(new ScourgeOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Its power and toughness are 20 minus the highest life total")
    void powerAndToughnessTrackHighestLifeTotal() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.setHand(player1, List.of(new ScourgeOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scourge = findPermanent(player1, "Scourge of the Skyclaves");
        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(10);

        harness.setLife(player2, 15);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(5);
    }

    @Test
    @DisplayName("An un-kicked cast does not trigger the life loss")
    void unKickedCastDoesNotTriggerLifeLoss() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.setHand(player1, List.of(new ScourgeOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(7);
    }
}
