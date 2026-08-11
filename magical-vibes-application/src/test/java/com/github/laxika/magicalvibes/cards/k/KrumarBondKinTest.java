package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KrumarBondKinTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent krumar = findPermanent(player1, "Krumar Bond-Kin");
        assertThat(krumar.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int krumarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(krumar);
        harness.turnFaceUp(player1, krumarIndex);
        harness.passBothPriorities();

        assertThat(krumar.isFaceDown()).isFalse();
    }
}
