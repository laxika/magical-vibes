package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImplementOfImprovement.class, GrizzlyBears.class, Shatter.class})
class ImplementOfImprovementTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it gains 2 life and draws a card")
    void sacrificingItGainsLifeAndDrawsCard() {
        harness.addToBattlefield(player1, new ImplementOfImprovement());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Implement of Improvement");
    }

    @Test
    @DisplayName("Draws a card when it is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new ImplementOfImprovement());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Implement of Improvement");
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the draw resolves before life gain")
    void drawResolvesBeforeLifeGain() {
        harness.addToBattlefield(player1, new ImplementOfImprovement());
        harness.setLibrary(player1, List.of(new ImplementOfImprovement()));
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Implement of Improvement");
        harness.assertInGraveyard(player1, "Implement of Improvement");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The last controller draws when sacrificing an opponent-owned Implement")
    void lastControllerDrawsRatherThanOwner() {
        var implement = new ImplementOfImprovement();
        implement.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, implement);
        harness.setLibrary(player1, List.of(new ImplementOfImprovement()));
        harness.setLibrary(player2, List.of(new ImplementOfImprovement()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int ownerHandSize = gd.playerHands.get(player1.getId()).size();
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Implement of Improvement");
        harness.assertNotInGraveyard(player2, "Implement of Improvement");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownerHandSize);
        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
    }
}
