package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartwoodDryadTest extends BaseCardTest {

    private void attacker(Card card, boolean shadow) {
        if (shadow) {
            card.setKeywords(Set.of(Keyword.SHADOW));
        }
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
    }

    private Permanent blocker(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private void beginBlocking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    @DisplayName("Heartwood Dryad can block a creature with shadow")
    void blocksShadowAttacker() {
        Permanent dryad = blocker(new HeartwoodDryad());
        attacker(new GrizzlyBears(), true);
        beginBlocking();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dryad.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Heartwood Dryad still blocks creatures without shadow")
    void blocksNormalAttacker() {
        Permanent dryad = blocker(new HeartwoodDryad());
        attacker(new GrizzlyBears(), false);
        beginBlocking();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dryad.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without the ability still can't block a creature with shadow")
    void plainBlockerCannotBlockShadow() {
        blocker(new GrizzlyBears());
        attacker(new GrizzlyBears(), true);
        beginBlocking();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
