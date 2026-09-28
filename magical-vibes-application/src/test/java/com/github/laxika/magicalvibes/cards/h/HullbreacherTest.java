package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hullbreacher.class, Forest.class, GrizzlyBears.class, Island.class, Peek.class})
class HullbreacherTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces an opponent's extra draw with a Treasure token")
    void replacesOpponentExtraDrawWithTreasure() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Peek()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not replace the first draw during an opponent's draw step")
    void doesNotReplaceFirstDrawStepDraw() {
        harness.addToBattlefield(player1, new Hullbreacher());
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not replace the controller's own draw")
    void doesNotReplaceControllerDraw() {
        harness.addToBattlefield(player1, new Hullbreacher());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
