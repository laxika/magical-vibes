package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TrainedCondor.class, GrizzlyBears.class, Shock.class})
class TrainedCondorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to another creature you control, even a non-attacker")
    void grantsFlyingToAnotherCreatureYouControl() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TrainedCondor());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(other.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TrainedCondor());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(other.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent condor = addCreatureReady(player1, new TrainedCondor());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, condor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TrainedCondor());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another attacking creature can receive flying")
    void grantsFlyingToAnotherAttacker() {
        addCreatureReady(player1, new TrainedCondor());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking without another creature does not require an impossible target")
    void attackingAloneDoesNotRequireATarget() {
        addCreatureReady(player1, new TrainedCondor());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves even if the Condor dies in response")
    void triggerResolvesAfterSourceDies() {
        Permanent condor = addCreatureReady(player1, new TrainedCondor());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, other.getId());
        harness.castInstant(player2, 0, condor.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Trained Condor");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A removed target does not cause the trigger to grant flying elsewhere")
    void removedTargetDoesNotGrantFlyingToAnotherCreature() {
        addCreatureReady(player1, new TrainedCondor());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
