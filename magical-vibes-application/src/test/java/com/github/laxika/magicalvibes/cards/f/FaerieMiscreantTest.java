package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaerieMiscreantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control another Faerie Miscreant")
    void etbDrawsWithAnotherCopy() {
        harness.addToBattlefield(player1, new FaerieMiscreant());
        int handBefore = castFaerieMiscreant();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB does NOT trigger without another Faerie Miscreant")
    void etbDoesNotTriggerAlone() {
        int handBefore = castFaerieMiscreant();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertOnBattlefield(player1, "Faerie Miscreant");
    }

    @Test
    @DisplayName("ETB does NOT trigger for a differently named creature")
    void etbDoesNotTriggerForOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int handBefore = castFaerieMiscreant();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB does NOT trigger when the opponent controls the other copy")
    void etbDoesNotTriggerForOpponentCopy() {
        harness.addToBattlefield(player2, new FaerieMiscreant());
        int handBefore = castFaerieMiscreant();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB draws nothing if the other copy leaves before resolution")
    void etbFizzlesWhenOtherCopyRemoved() {
        harness.addToBattlefield(player1, new FaerieMiscreant());
        int handBefore = castFaerieMiscreant();
        harness.passBothPriorities(); // resolve creature spell — trigger on stack

        gd.playerBattlefields.get(player1.getId()).removeFirst(); // the pre-existing copy

        harness.passBothPriorities(); // resolve ETB trigger — condition no longer met

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private int castFaerieMiscreant() {
        harness.setHand(player1, List.of(new FaerieMiscreant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        return gd.playerHands.get(player1.getId()).size();
    }
}
