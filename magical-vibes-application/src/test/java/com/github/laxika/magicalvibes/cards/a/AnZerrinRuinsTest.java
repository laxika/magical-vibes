package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnZerrinRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Creature of the chosen type stays tapped through its controller's untap step")
    void chosenTypeDoesNotUntap() {
        addRuins(player1, CardSubtype.GIANT);
        Permanent giant = addReady(player1, new HillGiant());
        giant.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature of another type untaps normally")
    void otherTypeUntaps() {
        addRuins(player1, CardSubtype.GIANT);
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locks opponents' creatures of the chosen type too")
    void affectsOpponentCreatures() {
        addRuins(player1, CardSubtype.GIANT);
        Permanent opponentGiant = addReady(player2, new HillGiant());
        opponentGiant.tap();

        advanceToNextTurn(player1);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once the Ruins leaves the battlefield, the lock is gone")
    void untapsAfterRuinsLeaves() {
        Permanent ruins = addRuins(player1, CardSubtype.GIANT);
        Permanent giant = addReady(player1, new HillGiant());
        giant.tap();

        gd.playerBattlefields.get(player1.getId()).remove(ruins);

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting the Ruins prompts for a creature type, which then locks matching creatures")
    void choosesTypeOnEnter() {
        Permanent giant = addReady(player1, new HillGiant());
        giant.tap();

        harness.setHand(player1, List.of(new AnZerrinRuins()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GIANT");

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    private Permanent addRuins(Player player, CardSubtype chosen) {
        Permanent ruins = new Permanent(new AnZerrinRuins());
        ruins.setChosenSubtype(chosen);
        gd.playerBattlefields.get(player.getId()).add(ruins);
        return ruins;
    }

    private Permanent addReady(Player player, Card card) {
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
