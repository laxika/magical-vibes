package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Fragmentize;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.r.RushOfVitality;
import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarionetteMaster.class, PropheticPrism.class, Fragmentize.class, TidyConclusion.class,
        RushOfVitality.class})
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
        harness.addToBattlefield(player1, new PropheticPrism());
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
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setLife(player2, 20);

        destroyArtifact(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Fabricate asks whether to put counters on the creature as the trigger resolves")
    void fabricateChoiceIsMadeOnResolution() {
        harness.setHand(player1, List.of(new MarionetteMaster()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Marionette Master")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(findPermanent(player1, "Marionette Master")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Life loss uses Marionette Master's power at resolution")
    void lifeLossUsesPowerAtResolution() {
        castMarionetteMaster(0);
        harness.addToBattlefield(player1, new PropheticPrism());
        destroyArtifact(player1);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player1, List.of(new RushOfVitality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Marionette Master"));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Fabricate creates Servos if Marionette Master leaves before the trigger resolves")
    void fabricateCreatesServosWhenSourceLeaves() {
        harness.setHand(player1, List.of(new MarionetteMaster()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Marionette Master"));
        harness.assertInGraveyard(player1, "Marionette Master");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .hasSize(3);
    }

    @Test
    @DisplayName("Destroying a Servo token triggers life loss")
    void servoDeathTriggersLifeLoss() {
        castMarionetteMaster(1);
        harness.setHand(player2, List.of(new Fragmentize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, harness.getPermanentId(player1, "Servo"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Life loss uses last known power if Marionette Master leaves in response")
    void lifeLossUsesLastKnownPower() {
        castMarionetteMaster(0);
        harness.addToBattlefield(player1, new PropheticPrism());
        destroyArtifact(player1);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Marionette Master"));
        harness.assertInGraveyard(player1, "Marionette Master");
        harness.setLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    private void castMarionetteMaster(int mode) {
        harness.setHand(player1, List.of(new MarionetteMaster()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyArtifact(com.github.laxika.magicalvibes.model.Player artifactController) {
        harness.setHand(player2, List.of(new Fragmentize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0,
                harness.getPermanentId(artifactController, "Prophetic Prism"));
    }
}
