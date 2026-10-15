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
    @DisplayName("Resolving the targeted trigger prompts for the optional action")
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

    @Test
    @DisplayName("Pestermite can target itself when it is the only permanent")
    void canTargetItself() {
        castPestermite();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Pestermite"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The action uses the target's state at resolution")
    void targetTappedBeforeResolutionIsUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        target.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the optional action leaves a tapped target tapped")
    void decliningLeavesTappedTargetUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        target.tap();
        castPestermite();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger does not resolve when its target leaves the battlefield")
    void removedTargetStopsResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves after Pestermite leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        castPestermite();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting Pestermite during the opponent's end step")
    void canCastDuringOpponentsEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.castFromHand(player1, new Pestermite(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Pestermite");
        assertThat(target.isTapped()).isTrue();
    }
}
