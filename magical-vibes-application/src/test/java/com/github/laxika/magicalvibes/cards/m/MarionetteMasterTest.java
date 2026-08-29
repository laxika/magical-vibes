package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarionetteMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts three +1/+1 counters on Marionette Master")
    void fabricateCountersMode() {
        castMarionetteMaster(0);

        Permanent master = findPermanent(player1, "Marionette Master");
        assertThat(master.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(6);
    }

    @Test
    @DisplayName("Fabricate mode creates three Servo artifact creature tokens")
    void fabricateServoMode() {
        castMarionetteMaster(1);

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(3);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("An artifact you control makes a target opponent lose Marionette Master's power")
    void ownArtifactMakesTargetOpponentLoseSourcePower() {
        castMarionetteMaster(0);
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player2, 20);

        destroyArtifact(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger Marionette Master")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new MarionetteMaster());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        destroyArtifact(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    private void castMarionetteMaster(int mode) {
        harness.setHand(player1, List.of(new MarionetteMaster()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyArtifact(com.github.laxika.magicalvibes.model.Player artifactController) {
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0,
                harness.getPermanentId(artifactController, "Mind Stone"));
        harness.passBothPriorities();
    }
}
