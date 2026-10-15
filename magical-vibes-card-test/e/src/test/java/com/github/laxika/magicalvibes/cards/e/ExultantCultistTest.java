package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DragUnder;
import com.github.laxika.magicalvibes.cards.g.GalvanicBombardment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExultantCultist.class, GalvanicBombardment.class, DragUnder.class})
class ExultantCultistTest extends BaseCardTest {

    @Test
    void drawsCardWhenItDies() {
        ExultantCultist drawnCard = new ExultantCultist();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new ExultantCultist());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GalvanicBombardment()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Exultant Cultist");
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertInGraveyard(player1, "Exultant Cultist");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void opponentDrawsForTheirOwnCultistsDeath() {
        ExultantCultist drawnCard = new ExultantCultist();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addToBattlefield(player2, new ExultantCultist());
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Exultant Cultist"));

        harness.assertInGraveyard(player2, "Exultant Cultist");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returningToHandDoesNotTriggerDeathAbility() {
        ExultantCultist cultist = new ExultantCultist();
        ExultantCultist undrawnCard = new ExultantCultist();
        ExultantCultist opponentsDraw = new ExultantCultist();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.addToBattlefield(player1, cultist);
        harness.setHand(player2, List.of(new DragUnder()));
        harness.setLibrary(player2, List.of(opponentsDraw));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player2, 0,
                harness.getPermanentId(player1, "Exultant Cultist"));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cultist);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsDraw);
        assertThat(gd.stack).isEmpty();
    }
}
