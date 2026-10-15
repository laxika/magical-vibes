package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RetreatToHagra.class, Forest.class, GrizzlyBears.class})
class RetreatToHagraTest extends BaseCardTest {

    private static final String BOOST_MODE =
            "Target creature gets +1/+0 and gains deathtouch until end of turn.";
    private static final String DRAIN_MODE =
            "Each opponent loses 1 life and you gain 1 life.";

    @Test
    @DisplayName("Landfall boost mode pumps a target creature and grants deathtouch")
    void boostMode() {
        harness.addToBattlefield(player1, new RetreatToHagra());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_MODE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Landfall boost mode expires at end of turn")
    void boostModeExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new RetreatToHagra());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_MODE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Landfall drain mode makes each opponent lose 1 life and gains 1 life")
    void drainMode() {
        harness.addToBattlefield(player1, new RetreatToHagra());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DRAIN_MODE);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Boost mode cannot target a noncreature permanent")
    void boostModeRejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new RetreatToHagra());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, BOOST_MODE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new RetreatToHagra());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Drain uses the landfall ability's controller")
    void drainUsesAbilityController() {
        harness.addToBattlefield(player2, new RetreatToHagra());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);
        harness.handleListChoice(player2, DRAIN_MODE);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("A landfall ability resolves after Retreat to Hagra leaves the battlefield")
    void drainResolvesWithoutSource() {
        Permanent retreat = harness.addToBattlefieldAndReturn(player1, new RetreatToHagra());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, DRAIN_MODE);
        gd.playerBattlefields.get(player1.getId()).remove(retreat);
        gd.playerGraveyards.get(player1.getId()).add(retreat.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }
}
