package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheQueenOfDale.class, Forest.class, GrizzlyBears.class, MindStone.class})
class TheQueenOfDaleTest extends BaseCardTest {

    @Test
    @DisplayName("Recruit triggers for an opponent's first noncreature spell each turn")
    void recruitsOnFirstNoncreatureSpellEachTurn() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new MindStone()));
        addMana(player2, ManaColor.GREEN, 1);
        addMana(player2, ManaColor.COLORLESS, 3);
        prepareTurn(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        prepareTurn(player2);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Recruit does not create a Soldier when the discarded card is a land")
    void doesNotRecruitForLandDiscard() {
        harness.addToBattlefield(player1, new TheQueenOfDale());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new MindStone()));
        addMana(player2, ManaColor.COLORLESS, 2);
        prepareTurn(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void prepareTurn(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }
}
