package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrumarBondKin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuthlessRipperTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndTurnsFaceUpByRevealingABlackCard() {
        KrumarBondKin blackCard = new KrumarBondKin();
        harness.setHand(player1, List.of(new RuthlessRipper(), blackCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ripper = findPermanent(player1, "Ruthless Ripper");
        harness.setLife(player2, 20);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ripper), 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(ripper.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotTurnFaceUpWithoutRevealingABlackCard() {
        harness.setHand(player1, List.of(new RuthlessRipper(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ripper = findPermanent(player1, "Ruthless Ripper");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ripper), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be black card");
        assertThat(ripper.isFaceDown()).isTrue();
    }
}
