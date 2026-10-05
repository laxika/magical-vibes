package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LutriPauperOtter.class, GrizzlyBears.class, Island.class, Unsummon.class})
class LutriPauperOtterTest extends BaseCardTest {

    @Test
    void enteringDiscardsHandAndDrawsThreeCards() {
        LutriPauperOtter lutri = new LutriPauperOtter();
        GrizzlyBears discarded = new GrizzlyBears();
        List<Card> drawn = List.of(new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(lutri, discarded));
        harness.setLibrary(player1, drawn);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void drawsThreeEvenWhenHandIsEmptyAndLeavesOpponentUnaffected() {
        List<Card> drawn = List.of(new Island(), new Island(), new Island());
        GrizzlyBears opponentCard = new GrizzlyBears();
        Island opponentLibraryCard = new Island();
        harness.setHand(player1, List.of(new LutriPauperOtter()));
        harness.setLibrary(player1, drawn);
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
    }

    @Test
    void triggerDiscardsReturnedLutriAndStillDrawsAfterItLeavesBattlefield() {
        LutriPauperOtter lutri = new LutriPauperOtter();
        GrizzlyBears discarded = new GrizzlyBears();
        List<Card> drawn = List.of(new Island(), new Island(), new Island());
        harness.setHand(player1, List.of(lutri, discarded));
        harness.setLibrary(player1, drawn);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Lutri, Pauper Otter"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lutri, Pauper Otter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discarded, lutri);
    }
}
