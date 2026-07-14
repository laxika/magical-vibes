package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KithkinZealotTest extends BaseCardTest {

    private void castKithkinZealot() {
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("Gains 1 life for each black and/or red permanent the target opponent controls")
    void gainsLifePerBlackAndRedPermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new BlackKnight());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        castKithkinZealot();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Permanents that are neither black nor red are ignored")
    void ignoresOtherColors() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castKithkinZealot();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Only the target opponent's permanents count, not the caster's")
    void ignoresCastersPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        castKithkinZealot();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains no life when the opponent controls no black or red permanents")
    void gainsNothingWithoutBlackOrRed() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castKithkinZealot();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
