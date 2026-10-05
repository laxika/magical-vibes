package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrismwakeMerrow.class, DevotedDruid.class, Forest.class})
class PrismwakeMerrowTest extends BaseCardTest {

    // ===== ETB trigger =====

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the chosen permanent")
    void etbTriggerGoesOnStack() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        castMerrow(druid.getId());
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(druid.getId());
    }

    @Test
    @DisplayName("Can choose Prismwake Merrow itself as the ETB target after it enters")
    void canTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new PrismwakeMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell

        Permanent merrow = findPermanent(player1, "Prismwake Merrow");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(merrow.getId());

        harness.handlePermanentChosen(player1, merrow.getId());
        harness.passBothPriorities(); // resolve the ETB trigger
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, merrow)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Resolving prompts the controller for a color choice")
    void resolvingPromptsColorChoice() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        castMerrow(druid.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    // ===== Choosing colors =====

    @Test
    @DisplayName("Choosing a single color makes the target only that color until end of turn")
    void singleColorReplacesColors() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        resolveMerrowAndChoose(druid.getId(), "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, druid)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing several colors makes the target all of those colors")
    void multipleColorsReplaceColors() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        resolveMerrowAndChoose(druid.getId(), "WHITE", "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, druid))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
    }

    @Test
    @DisplayName("Can target a noncreature permanent — a colorless land becomes the chosen color")
    void canTargetColorlessLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        resolveMerrowAndChoose(forest.getId(), "RED", "DONE");

        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid()); // green
        resolveMerrowAndChoose(druid.getId(), "BLUE", "DONE");
        assertThat(gqs.getEffectiveColors(gd, druid)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, druid)).containsExactly(CardColor.GREEN);
    }

    // ===== Helpers =====

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        resolveMerrowAndChoose(druid.getId(), "RED", "DONE");

        harness.assertOnBattlefield(player1, "Prismwake Merrow");
        assertThat(gqs.getEffectiveColors(gd, druid)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Choosing all five colors completes the choice without DONE")
    void canChooseAllFiveColors() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());

        resolveMerrowAndChoose(druid.getId(), "WHITE", "BLUE", "BLACK", "RED", "GREEN");

        assertThat(gqs.getEffectiveColors(gd, druid)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A later color change replaces all colors chosen by an earlier trigger")
    void laterColorChangeReplacesEarlierColors() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());
        resolveMerrowAndChoose(druid.getId(), "WHITE", "BLUE", "DONE");

        resolveMerrowAndChoose(druid.getId(), "BLACK", "RED", "DONE");

        assertThat(gqs.getEffectiveColors(gd, druid))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
    }

    private void castMerrow(UUID targetId) {
        harness.setHand(player1, List.of(new PrismwakeMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void resolveMerrowAndChoose(UUID targetId, String... choices) {
        castMerrow(targetId);
        resolveAllTriggers();
        for (String choice : choices) {
            harness.handleListChoice(player1, choice);
        }
    }
}
