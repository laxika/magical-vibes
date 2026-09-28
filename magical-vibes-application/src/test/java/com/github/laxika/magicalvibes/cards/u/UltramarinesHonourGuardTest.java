package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltramarinesHonourGuard.class, GrizzlyBears.class})
class UltramarinesHonourGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new UltramarinesHonourGuard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ultramarines Honour Guard")).hasSize(3);
        assertThat(findPermanents(player1, "Ultramarines Honour Guard"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void buffsOtherCreaturesYouControl() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addToBattlefield(player1, new UltramarinesHonourGuard());

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ultramarines Honour Guard does not buff itself")
    void doesNotBuffItself() {
        UltramarinesHonourGuard card = new UltramarinesHonourGuard();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent guard = findPermanent(player1, "Ultramarines Honour Guard");

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(10);
    }
}
