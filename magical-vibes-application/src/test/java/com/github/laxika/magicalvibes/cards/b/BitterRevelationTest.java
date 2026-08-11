package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BitterRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two of the top four cards into hand, the rest into the graveyard, and loses 2 life")
    void choosesTwoCardsAndLosesLife() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new Island();
        Card top4 = new Plains();
        GameData data = harness.getGameData();
        data.playerDecks.get(player1.getId()).clear();
        data.playerDecks.get(player1.getId()).addAll(List.of(top1, top2, top3, top4));

        harness.setHand(player1, List.of(new BitterRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId(), top3.getId()));

        assertThat(data.playerHands.get(player1.getId())).containsExactly(top1, top3);
        assertThat(data.playerDecks.get(player1.getId())).isEmpty();
        assertThat(data.playerGraveyards.get(player1.getId())).contains(top2, top4);
        assertThat(data.getLife(player1.getId())).isEqualTo(18);
    }
}
