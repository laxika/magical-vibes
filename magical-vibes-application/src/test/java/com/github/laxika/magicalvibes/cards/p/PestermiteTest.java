package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pestermite.class, HillcomberGiant.class, Forest.class})
class PestermiteTest extends BaseCardTest {

    private void castPestermite() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Pestermite(), "{2}{U}");
    }

    @Test
    @DisplayName("Resolving Pestermite prompts for the triggered ability's target")
    void resolvingPromptsForTarget() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting may prompts for target selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Hillcomber Giant"));
        harness.passBothPriorities(); // resolve triggered ability -> may prompt

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting and choosing an untapped permanent taps it")
    void tapsUntappedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        assertThat(target.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting and choosing a tapped permanent untaps it")
    void untapsTappedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        target.tap();
        assertThat(target.isTapped()).isTrue();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target any permanent, including a land")
    void canTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        assertThat(land.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a permanent controlled by Pestermite's controller")
    void canTargetOwnPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(land.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the may leaves the permanent unchanged")
    void decliningLeavesUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        assertThat(target.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve triggered ability -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Pestermite enters the battlefield")
    void pestermiteEntersBattlefield() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Pestermite");
    }

    @Test
    @DisplayName("ETB trigger uses the triggered-ability stack entry")
    void etbTriggerType() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> target choice
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Hillcomber Giant"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pestermite");
    }
}
