package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzasFilterTest extends BaseCardTest {

    @Test
    @DisplayName("Multicolored spells cost {2} less")
    void multicoloredSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new UrzasFilter());
        harness.setHand(player1, List.of(new QasaliAmbusher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Qasali Ambusher");
    }

    @Test
    @DisplayName("Monocolored spells are not reduced")
    void monocoloredSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new UrzasFilter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction also applies to an opponent's multicolored spells")
    void opponentMulticoloredSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new UrzasFilter());
        harness.setHand(player2, List.of(new QasaliAmbusher()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Qasali Ambusher");
    }
}
