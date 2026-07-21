package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GildedCerodonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a Desert on the battlefield makes the target creature unable to block")
    void attackWithDesertControlledMakesTargetCantBlock() {
        Permanent cerodon = addReady(player1, new GildedCerodon());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));
        Permanent blocker = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities(); // resolve the attack trigger

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attacking with a Desert card in the graveyard makes the target creature unable to block")
    void attackWithDesertInGraveyardMakesTargetCantBlock() {
        Permanent cerodon = addReady(player1, new GildedCerodon());
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        Permanent blocker = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities(); // resolve the attack trigger

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Without any Desert the intervening-if fails — the target creature is left able to block")
    void attackWithoutDesertDoesNotRestrictBlocking() {
        Permanent cerodon = addReady(player1, new GildedCerodon());
        Permanent blocker = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities(); // resolve the attack trigger — condition unmet, effect fizzles

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
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
