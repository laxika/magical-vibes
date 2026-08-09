package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FalseProphetTest extends BaseCardTest {

    @Test
    @DisplayName("When False Prophet dies, it exiles all creatures on both battlefields")
    void diesExilesAllCreatures() {
        harness.addToBattlefield(player1, new FalseProphet());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Island());

        killFalseProphet();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Island");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Hill Giant");
        harness.assertInGraveyard(player1, "False Prophet");
    }

    private void killFalseProphet() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID prophetId = harness.getPermanentId(player1, "False Prophet");
        harness.castInstant(player2, 0, prophetId);
        harness.passBothPriorities();
    }
}
