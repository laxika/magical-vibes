package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MurkfiendLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other green creatures you control")
    void buffsOtherGreen() {
        harness.addToBattlefield(player1, new MurkfiendLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent green = findPermanent(player1, "Grizzly Bears");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
    }

    @Test
    @DisplayName("Buffs other blue creatures you control")
    void buffsOtherBlue() {
        harness.addToBattlefield(player1, new MurkfiendLiege());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent blue = findPermanent(player1, "Air Elemental");

        // 4/4 base + 1/1 = 5/5
        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff itself or off-color creatures")
    void doesNotBuffItselfOrOffColor() {
        harness.addToBattlefield(player1, new MurkfiendLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent liege = findPermanent(player1, "Murkfiend Liege");
        Permanent red = findPermanent(player1, "Hill Giant");

        // Base 4/4, unaffected by its own "other" boosts (even though it is green and blue).
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
        // Red creature is neither green nor blue.
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(3);
    }

    @Test
    @DisplayName("Untaps green and blue creatures you control during opponent's untap step")
    void untapsGreenAndBlueOnOpponentUntap() {
        harness.addToBattlefield(player1, new MurkfiendLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent liege = findPermanent(player1, "Murkfiend Liege");
        Permanent green = findPermanent(player1, "Grizzly Bears");
        Permanent blue = findPermanent(player1, "Air Elemental");

        liege.tap();
        green.tap();
        blue.tap();

        advanceToNextTurn(player1); // player2 untap step

        assertThat(liege.isTapped()).isFalse();
        assertThat(green.isTapped()).isFalse();
        assertThat(blue.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap non-green/blue creatures you control")
    void doesNotUntapOffColor() {
        harness.addToBattlefield(player1, new MurkfiendLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent liege = findPermanent(player1, "Murkfiend Liege");
        Permanent red = findPermanent(player1, "Hill Giant");

        liege.tap();
        red.tap();

        advanceToNextTurn(player1); // player2 untap step

        assertThat(liege.isTapped()).isFalse();
        assertThat(red.isTapped()).isTrue(); // red creature is not untapped
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
