package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieMiscreant.class, LeafGilder.class, Disperse.class})
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
        harness.addToBattlefield(player1, new LeafGilder());
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

    @Test
    @DisplayName("ETB still draws if its source leaves while another copy remains")
    void etbDrawsAfterSourceReturnsToHand() {
        var other = harness.addToBattlefieldAndReturn(player1, new FaerieMiscreant());
        castFaerieMiscreant();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(other.getId()))
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        harness.assertInHand(player1, "Faerie Miscreant");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB draws exactly one card even with several other copies")
    void etbDrawsOnlyOneWithSeveralCopies() {
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        int handBefore = castFaerieMiscreant();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private int castFaerieMiscreant() {
        harness.castFromHand(player1, new FaerieMiscreant(), "{U}");
        return gd.playerHands.get(player1.getId()).size();
    }
}
