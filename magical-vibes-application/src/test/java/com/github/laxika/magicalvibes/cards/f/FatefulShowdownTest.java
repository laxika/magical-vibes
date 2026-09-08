package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FatefulShowdownTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the remaining hand, then discards and draws that many")
    void dealsDamageThenWheelsTheHand() {
        harness.setHand(player1, List.of(
                new FatefulShowdown(), new GrizzlyBears(), new Island(), new Mountain()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        addMana(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Island", "Island", "Island");
        harness.assertInGraveyard(player1, "Fateful Showdown");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Can deal the hand-size damage to a creature")
    void dealsDamageToCreatureTarget() {
        harness.addToBattlefield(player2, new WindDrake());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(
                new FatefulShowdown(), new GrizzlyBears(), new Island(), new Mountain()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wind Drake");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
