package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnHavvaInnTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when no green creatures are on the battlefield")
    void gainsOneWithNoGreenCreatures() {
        harness.setHand(player1, List.of(new AnHavvaInn()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Counts green creatures on every battlefield, plus one")
    void countsGreenCreaturesOnAllBattlefields() {
        harness.setHand(player1, List.of(new AnHavvaInn()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }
}
