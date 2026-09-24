package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LothoCorruptShirriff.class, Shock.class, DarkRitual.class})
class LothoCorruptShirriffTest extends BaseCardTest {

    @Test
    void losesLifeAndCreatesTreasureWhenControllerCastsSecondSpell() {
        addCreatureReady(player1, new LothoCorruptShirriff());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void triggersWhenAnOpponentCastsTheirSecondSpell() {
        addCreatureReady(player1, new LothoCorruptShirriff());
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
