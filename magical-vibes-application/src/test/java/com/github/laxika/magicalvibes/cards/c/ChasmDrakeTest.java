package com.github.laxika.magicalvibes.cards.c;

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

class ChasmDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to a target creature you control")
    void grantsFlyingToOwnCreature() {
        addReadyCreature(player1, new ChasmDrake());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new ChasmDrake());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadyCreature(player1, new ChasmDrake());
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
