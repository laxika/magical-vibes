package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheBazaar.class, AvenRiftwatcher.class, GossamerPhantasm.class})
class MagusOfTheBazaarTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards three cards")
    void drawsTwoThenDiscardsThree() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheBazaar());
        harness.setHand(player1, List.of(new AvenRiftwatcher(), new GossamerPhantasm(), new AvenRiftwatcher()));
        harness.setLibrary(player1, List.of(new GossamerPhantasm(), new AvenRiftwatcher()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(magus.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
