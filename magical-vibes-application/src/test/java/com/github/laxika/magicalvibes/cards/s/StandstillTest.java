package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StandstillTest extends BaseCardTest {

    private List<Card> tenCardLibrary() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cards.add(new SuntailHawk());
        }
        return cards;
    }

    @Test
    @DisplayName("When an opponent casts a spell, Standstill is sacrificed and its controller draws three")
    void opponentSpellSacrificesAndDraws() {
        harness.addToBattlefield(player1, new Standstill());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, tenCardLibrary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertNotOnBattlefield(player1, "Standstill");
        harness.assertInGraveyard(player1, "Standstill");
    }

    @Test
    @DisplayName("The caster's opponents draw, even when the Standstill controller casts the spell")
    void casterOpponentsDraw() {
        harness.addToBattlefield(player1, new Standstill());
        harness.setHand(player1, List.of(new GrizzlyBears(), new SuntailHawk()));
        harness.setLibrary(player2, tenCardLibrary());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore - 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 3);
    }

    @Test
    @DisplayName("If Standstill is gone before its trigger resolves, no cards are drawn")
    void noDrawWhenStandstillLeavesBeforeResolution() {
        Permanent standstill = harness.addToBattlefieldAndReturn(player1, new Standstill());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, tenCardLibrary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castCreature(player2, 0);
        gd.playerBattlefields.get(player1.getId()).remove(standstill);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
