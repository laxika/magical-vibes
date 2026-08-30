package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GruesomeFateTest extends BaseCardTest {

    @Test
    void eachOpponentLosesOneLifeForEachCreatureControllerControls() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AdantoVanguard());
        harness.setHand(player1, List.of(new GruesomeFate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void doesNothingWhenControllerControlsNoCreatures() {
        harness.setHand(player1, List.of(new GruesomeFate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
