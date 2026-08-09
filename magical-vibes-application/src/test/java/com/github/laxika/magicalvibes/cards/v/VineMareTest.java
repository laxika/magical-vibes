package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VineMareTest extends BaseCardTest {

    @Test
    @DisplayName("Vine Mare can't be blocked by black creatures")
    void cannotBeBlockedByBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new VineMare());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        prepareDeclareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vine Mare can be blocked by nonblack creatures")
    void canBeBlockedByNonblackCreature() {
        Permanent attacker = addCreatureReady(player1, new VineMare());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target Vine Mare with spells")
    void opponentCannotTargetWithSpells() {
        Permanent vineMare = addCreatureReady(player1, new VineMare());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, vineMare.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private void prepareDeclareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
