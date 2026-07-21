package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AerialGuideTest extends BaseCardTest {

    // ===== Attack trigger grants flying to another attacking creature =====

    @Test
    @DisplayName("Attacking grants flying to another attacking creature")
    void grantsFlyingToAnotherAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new AerialGuide());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new AerialGuide());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherAttacker.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent aerialGuide = addReadyCreature(player1, new AerialGuide());
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, aerialGuide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new AerialGuide());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        // Only Aerial Guide (0) and the first Grizzly Bears (1) attack; the third stays back.
        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
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
