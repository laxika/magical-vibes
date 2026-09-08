package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AerialGuide;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

class RocChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to an attacking creature without flying")
    void grantsFlyingToAttackingCreatureWithoutFlying() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new RocCharger());
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

        addReadyCreature(player1, new RocCharger());
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

    @Test
    @DisplayName("Cannot target a creature that already has flying")
    void cannotTargetCreatureThatAlreadyHasFlying() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new RocCharger());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent flyingAttacker = addReadyCreature(player1, new AerialGuide());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, flyingAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new RocCharger());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
