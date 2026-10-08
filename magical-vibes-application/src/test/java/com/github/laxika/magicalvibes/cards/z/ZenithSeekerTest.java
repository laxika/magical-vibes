package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZenithSeeker.class, Censor.class, DuneBeetle.class, TormentingVoice.class})
class ZenithSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("An ordinary discard can grant flying to an opponent's creature")
    void ordinaryDiscardCanTargetOpposingCreature() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new TormentingVoice(), new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle(), new DuneBeetle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, beetle.getId());
        harness.passBothPriorities();

        assertThat(beetle.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cycling triggers once and grants flying before the cycling draw")
    void cyclingTriggerResolvesBeforeDraw() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        DuneBeetle drawnCard = new DuneBeetle();
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, beetle.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(beetle.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent cycling a card does not trigger Zenith Seeker")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        DuneBeetle drawnCard = new DuneBeetle();
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
        assertThat(beetle.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cycling a card queues a target-creature choice for the flying grant")
    void cyclingQueuesTargetChoice() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Cycling is a discard (CR 702.29a), so cycling Censor triggers "target creature gains flying".
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving the trigger grants flying to the chosen creature")
    void grantsFlyingToChosenCreature() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // discard trigger awaits target

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the flying grant

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("The trigger targets a creature only, not a player")
    void triggerCannotTargetPlayer() {
        harness.addToBattlefield(player1, new ZenithSeeker());
        harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // discard trigger awaits target

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
