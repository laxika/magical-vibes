package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaringArchaeologist.class, AdelizTheCinderWind.class, GrizzlyBears.class, Spellbook.class, HistoryOfBenalia.class})
class DaringArchaeologistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers may ability prompt when artifact is in graveyard")
    void etbTriggersMayPrompt() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castAndResolve();
        chooseArtifactTarget();
        harness.passBothPriorities(); // resolve MayEffect from stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may ability returns artifact from graveyard to hand")
    void acceptingMayReturnsArtifact() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castAndAcceptMay();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Declining may ability does not return artifact")
    void decliningMayDoesNotReturnArtifact() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castAndResolve();
        chooseArtifactTarget();
        harness.passBothPriorities(); // resolve MayEffect
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("ETB does not offer non-artifact cards from graveyard")
    void etbDoesNotOfferNonArtifact() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndResolve();

        // No artifact in graveyard — no graveyard choice
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void etbNoEffectEmptyGraveyard() {
        castAndResolve();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting an artifact puts a +1/+1 counter on Daring Archaeologist")
    void artifactSpellPutsCounter() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Daring Archaeologist"));

        // Resolve triggered ability
        harness.passBothPriorities();

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a legendary creature puts a +1/+1 counter on Daring Archaeologist")
    void legendarySpellPutsCounter() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.castFromHand(player1, new AdelizTheCinderWind(), "{1}{U}{R}");

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Daring Archaeologist"));

        // Resolve triggered ability
        harness.passBothPriorities();

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-historic spell does not put a counter")
    void nonHistoricDoesNotPutCounter() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent casting historic spell does not trigger controller's Daring Archaeologist")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new DaringArchaeologist());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        // Only artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Multiple historic spells put multiple counters")
    void multipleHistoricSpellsPutMultipleCounters() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (+1/+1 counter)
        harness.passBothPriorities(); // resolve Spellbook

        // Cast second artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (+1/+1 counter)

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a Saga puts a counter on Daring Archaeologist before the Saga resolves")
    void sagaSpellPutsCounter() {
        Permanent archaeologist = harness.addToBattlefieldAndReturn(player1, new DaringArchaeologist());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "History of Benalia");
    }

    @Test
    @DisplayName("An opponent's artifact cannot supply the ETB target")
    void opponentGraveyardCannotSupplyTarget() {
        harness.setGraveyard(player2, List.of(new Spellbook()));
        castAndResolve();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertNotInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("An artifact put in the graveyard after entry cannot become the ETB target")
    void artifactArrivingAfterEntryCannotBeReturned() {
        castAndResolve();
        harness.setGraveyard(player1, List.of(new Spellbook()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not replaced by another artifact")
    void missingTargetDoesNotReturnAnotherArtifact() {
        Spellbook target = new Spellbook();
        Spellbook other = new Spellbook();
        harness.setGraveyard(player1, List.of(target, other));
        castAndResolve();
        chooseArtifactTarget();
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Putting an artifact onto the battlefield without casting does not trigger")
    void artifactEnteringWithoutCastDoesNotTrigger() {
        Permanent archaeologist = harness.addToBattlefieldAndReturn(player1, new DaringArchaeologist());
        harness.enterBattlefieldAndReturn(player1, new Spellbook());

        assertThat(gd.stack).isEmpty();
        assertThat(archaeologist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void chooseArtifactTarget() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DaringArchaeologist(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature spell
    }

    private void castAndAcceptMay() {
        castAndResolve();
        chooseArtifactTarget();
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    private Permanent findArchaeologist(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Daring Archaeologist"))
                .findFirst().orElse(null);
    }
}
