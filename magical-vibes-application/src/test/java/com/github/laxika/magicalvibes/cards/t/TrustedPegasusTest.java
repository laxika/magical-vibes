package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

class TrustedPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to an attacking creature without flying")
    void grantsFlyingToAttackingCreatureWithoutFlying() {
        addReadyCreature(player1, new TrustedPegasus());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void grantedFlyingWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new TrustedPegasus());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an attacking creature that already has flying")
    void cannotTargetCreatureWithFlying() {
        addReadyCreature(player1, new TrustedPegasus());
        Permanent flyer = addReadyCreature(player1, new SuntailHawk());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addReadyCreature(player1, new TrustedPegasus());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
