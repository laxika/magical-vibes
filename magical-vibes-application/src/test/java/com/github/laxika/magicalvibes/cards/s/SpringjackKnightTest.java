package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringjackKnightTest extends BaseCardTest {

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackTriggersTargetSelection() {
        addReadyKnight(player1);
        addReadyCreature(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Choosing target puts the clash trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent knight = addReadyKnight(player1);
        Permanent ally = addReadyCreature(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Springjack Knight")
                        && se.getTargetId().equals(ally.getId())
                        && se.getSourcePermanentId().equals(knight.getId()));
    }

    // ===== Won clash — target gains double strike =====

    @Test
    @DisplayName("Winning the clash grants double strike to the target creature")
    void wonClashGrantsDoubleStrike() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        addReadyKnight(player1);
        Permanent ally = addReadyCreature(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    // ===== Lost clash — no grant =====

    @Test
    @DisplayName("Losing the clash grants nothing")
    void lostClashGrantsNothing() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        addReadyKnight(player1);
        Permanent ally = addReadyCreature(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Tie — a clash is only won on a strictly greater mana value (CR 701.29c) =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so nothing is granted")
    void tiedClashGrantsNothing() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        addReadyKnight(player1);
        Permanent ally = addReadyCreature(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        addReadyKnight(player1);
        Permanent ally = addReadyCreature(player1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyKnight(Player player) {
        Permanent perm = new Permanent(new SpringjackKnight());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
