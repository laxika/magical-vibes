package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WrathOfMaritLageTest extends BaseCardTest {

    @Test
    @DisplayName("Entering taps all red creatures but leaves non-red creatures untapped")
    void etbTapsRedCreatures() {
        Permanent redGiant = addReady(player1, new HillGiant());   // Red 3/3
        Permanent greenBears = addReady(player2, new GrizzlyBears()); // Green 2/2

        harness.setHand(player1, List.of(new WrathOfMaritLage()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);

        harness.passBothPriorities(); // enchantment resolves → ETB trigger on stack
        harness.passBothPriorities(); // ETB trigger resolves

        assertThat(redGiant.isTapped()).isTrue();
        assertThat(greenBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped red creature does not untap while Wrath of Marit Lage is out")
    void redCreatureStaysTapped() {
        harness.addToBattlefield(player1, new WrathOfMaritLage());
        Permanent redGiant = addReady(player1, new HillGiant());
        redGiant.tap();

        advanceToNextTurn(player2);

        assertThat(redGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-red creature untaps normally")
    void nonRedCreatureUntaps() {
        harness.addToBattlefield(player1, new WrathOfMaritLage());
        Permanent greenBears = addReady(player1, new GrizzlyBears());
        greenBears.tap();

        advanceToNextTurn(player2);

        assertThat(greenBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' red creatures during their untap step")
    void affectsOpponentRedCreatures() {
        harness.addToBattlefield(player1, new WrathOfMaritLage());
        Permanent opponentGiant = addReady(player2, new HillGiant());
        opponentGiant.tap();

        advanceToNextTurn(player1); // player2's untap step

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Wrath of Marit Lage leaves, red creatures untap again")
    void untapsAfterEnchantmentLeaves() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new WrathOfMaritLage());
        Permanent redGiant = addReady(player1, new HillGiant());
        redGiant.tap();

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        advanceToNextTurn(player2);

        assertThat(redGiant.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
