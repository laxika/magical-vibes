package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FathomSeer.class, Island.class, GrizzlyBears.class})
class FathomSeerTest extends BaseCardTest {

    @Test
    void returnsTwoIslandsAndDrawsTwoCardsWhenTurnedFaceUp() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new FathomSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent fathomSeer = findPermanent(player1, "Fathom Seer");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fathomSeer),
                List.of(firstIsland.getId(), secondIsland.getId()));
        harness.passBothPriorities();

        assertThat(fathomSeer.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstIsland, secondIsland);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstIsland.getCard(), secondIsland.getCard(), firstDraw, secondDraw);
    }
}
