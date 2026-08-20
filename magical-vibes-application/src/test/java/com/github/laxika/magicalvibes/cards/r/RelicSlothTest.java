package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicSlothTest extends BaseCardTest {

    @Test
    @DisplayName("Relic Sloth has vigilance and menace")
    void hasKeywords() {
        Permanent sloth = addCreatureReady(player1, new RelicSloth());

        assertThat(gqs.hasKeyword(gd, sloth, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sloth, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Relic Sloth untapped after attacking")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent sloth = addCreatureReady(player1, new RelicSloth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(sloth.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Menace prevents Relic Sloth from being blocked by one creature")
    void menaceRequiresTwoBlockers() {
        Permanent sloth = addCreatureReady(player1, new RelicSloth());
        sloth.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(sloth);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }
}
