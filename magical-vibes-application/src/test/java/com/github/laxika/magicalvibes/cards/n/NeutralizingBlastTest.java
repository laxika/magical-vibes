package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeutralizingBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a multicolored spell")
    void countersMulticoloredSpell() {
        WoollyThoctar thoctar = new WoollyThoctar();
        harness.setHand(player1, List.of(thoctar));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, thoctar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Woolly Thoctar");
        harness.assertNotOnBattlefield(player1, "Woolly Thoctar");
    }

    @Test
    @DisplayName("Cannot target a monocolored spell")
    void cannotTargetMonocoloredSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
