package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChasmDrake.class, RuneclawBear.class, Unsummon.class})
class ChasmDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to a target creature you control")
    void grantsFlyingToOwnCreature() {
        addCreatureReady(player1, new ChasmDrake());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ChasmDrake());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

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
        addCreatureReady(player1, new ChasmDrake());
        Permanent opponentBears = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attacking Drake can target itself")
    void canTargetItself() {
        Permanent drake = addCreatureReady(player1, new ChasmDrake());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, drake.getId());
        harness.passBothPriorities();

        assertThat(drake.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Drake that does not attack does not grant flying")
    void doesNotTriggerWhenAnotherCreatureAttacks() {
        addCreatureReady(player1, new ChasmDrake());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves even if the Drake leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent drake = addCreatureReady(player1, new ChasmDrake());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, bears.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, drake.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(drake);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution does not gain flying")
    void doesNotGrantFlyingToRemovedTarget() {
        addCreatureReady(player1, new ChasmDrake());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, bears.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(gd.stack).isEmpty();
    }
}
