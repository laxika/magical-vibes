package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClockworkDroid.class})
class ClockworkDroidTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyDroid(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting makes the Droid unblockable, scries, and skips its next untap")
    void exertAppliesAllEffects() {
        Permanent droid = addReadyDroid(player1);
        harness.setLibrary(player1, List.of(new ClockworkDroid()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(droid.isTapped()).isTrue();
        assertThat(droid.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves the Droid blockable and untap unaffected")
    void decliningExertDoesNothing() {
        Permanent droid = addReadyDroid(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasCantBeBlocked(gd, droid)).isFalse();
        assertThat(droid.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is chosen during attack declaration before players receive priority")
    void exertChoicePrecedesAttackTriggerResolution() {
        addReadyDroid(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exert skips one untap step even with an empty library")
    void emptyLibraryStillSkipsExactlyOneUntap() {
        Permanent droid = addReadyDroid(player1);
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(droid.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(droid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void newControllerCanUntapExertedDroid() {
        Permanent droid = addReadyDroid(player1);
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(droid);
        gd.playerBattlefields.get(player2.getId()).add(droid);
        harness.performUntapStep(player2);

        assertThat(droid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert expires at the exerting player's untap step while another player controls the Droid")
    void exertExpiresWhileControlledByOpponent() {
        Permanent droid = addReadyDroid(player1);
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(droid);
        gd.playerBattlefields.get(player2.getId()).add(droid);
        harness.performUntapStep(player1);
        gd.playerBattlefields.get(player2.getId()).remove(droid);
        gd.playerBattlefields.get(player1.getId()).add(droid);
        harness.performUntapStep(player1);

        assertThat(droid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Scry may put the top card on the bottom without looking at the second card")
    void exertScryCanBottomTopCard() {
        addReadyDroid(player1);
        ClockworkDroid top = new ClockworkDroid();
        ClockworkDroid second = new ClockworkDroid();
        harness.setLibrary(player1, List.of(top, second));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
    }

    private Permanent addReadyDroid(Player player) {
        return addCreatureReady(player, new ClockworkDroid());
    }
}
