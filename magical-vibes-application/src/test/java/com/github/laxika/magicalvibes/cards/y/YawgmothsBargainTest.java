package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YawgmothsBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 1 life draws a card and the ability can be activated repeatedly")
    void paysLifeToDrawCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new YawgmothsBargain());
        harness.setLife(player1, 20);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.addToBattlefield(player1, new YawgmothsBargain());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
