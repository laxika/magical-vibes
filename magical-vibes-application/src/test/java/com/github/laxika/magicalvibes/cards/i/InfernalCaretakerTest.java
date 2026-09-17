package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinTurncoat;
import com.github.laxika.magicalvibes.cards.z.ZombieBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalCaretaker.class, ZombieBrute.class, GoblinTurncoat.class})
class InfernalCaretakerTest extends BaseCardTest {

    @Test
    void returnsAllZombieCardsFromAllGraveyardsToTheirOwnersHands() {
        Card ownZombie = new ZombieBrute();
        Card ownNonZombie = new GoblinTurncoat();
        Card opponentZombie = new ZombieBrute();
        Card opponentNonZombie = new GoblinTurncoat();
        harness.setGraveyard(player1, List.of(ownZombie, ownNonZombie));
        harness.setGraveyard(player2, List.of(opponentZombie, opponentNonZombie));
        harness.setHand(player1, List.of(new InfernalCaretaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent caretaker = findPermanent(player1, "Infernal Caretaker");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(caretaker));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownZombie);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentZombie);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownNonZombie);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentNonZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownNonZombie);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentNonZombie);
    }

    @Test
    void leavesGraveyardsUnchangedWhenNoZombieCardsAreAvailable() {
        Card ownNonZombie = new GoblinTurncoat();
        Card opponentNonZombie = new GoblinTurncoat();
        harness.setGraveyard(player1, List.of(ownNonZombie));
        harness.setGraveyard(player2, List.of(opponentNonZombie));
        harness.setHand(player1, List.of(new InfernalCaretaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent caretaker = findPermanent(player1, "Infernal Caretaker");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(caretaker));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownNonZombie);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentNonZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownNonZombie);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentNonZombie);
    }
}
