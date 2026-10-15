package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.SanguinaryPriest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltramarinesHonourGuard.class, SanguinaryPriest.class})
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SanguinaryPriest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SanguinaryPriest());

        harness.addToBattlefield(player1, new UltramarinesHonourGuard());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ultramarines Honour Guard does not buff itself")
    void doesNotBuffItself() {
        UltramarinesHonourGuard card = new UltramarinesHonourGuard();
        card.setPower(10);
        card.setToughness(10);
        Permanent guard = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(10);
    }

    @Test
    @DisplayName("Squad does not trigger when the creature enters without being cast")
    void squadDoesNotTriggerWithoutPayment() {
        harness.enterBattlefieldAndReturn(player1, new UltramarinesHonourGuard());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Ultramarines Honour Guard")).hasSize(1);
    }

    @Test
    @DisplayName("Casting without paying squad creates no token copies")
    void castingWithoutSquadCreatesNoCopies() {
        harness.setHand(player1, List.of(new UltramarinesHonourGuard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ultramarines Honour Guard")).hasSize(1);
        assertThat(findPermanents(player1, "Ultramarines Honour Guard"))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Squad copies retain their anthem and boost each other and the original")
    void squadCopiesStackTheirAnthems() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SanguinaryPriest());
        harness.setHand(player1, List.of(new UltramarinesHonourGuard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ultramarines Honour Guard"))
                .hasSize(3)
                .allSatisfy(guard -> {
                    assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
                });
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(7);
    }
}
