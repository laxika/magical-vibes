package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrismwakeMerrowTest extends BaseCardTest {

    // ===== ETB trigger =====

    @Test
    @DisplayName("ETB trigger goes on the stack targeting the chosen permanent")
    void etbTriggerGoesOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMerrow(bears.getId());
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving prompts the controller for a color choice")
    void resolvingPromptsColorChoice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMerrow(bears.getId());
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    // ===== Choosing colors =====

    @Test
    @DisplayName("Choosing a single color makes the target only that color until end of turn")
    void singleColorReplacesColors() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveMerrowAndChoose(bears.getId(), "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing several colors makes the target all of those colors")
    void multipleColorsReplaceColors() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveMerrowAndChoose(bears.getId(), "WHITE", "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears))
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // green
        resolveMerrowAndChoose(bears.getId(), "BLUE", "DONE");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);

        // Simulate end-of-turn cleanup: the floating CR 613 layer-5 setter expires alongside the
        // Permanent-level modifier reset, so the creature reverts to its printed color.
        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    // ===== Helpers =====

    private void castMerrow(UUID targetId) {
        harness.setHand(player1, List.of(new PrismwakeMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void resolveMerrowAndChoose(UUID targetId, String... choices) {
        castMerrow(targetId);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger -> begins the color choice
        for (String choice : choices) {
            harness.handleListChoice(player1, choice);
        }
    }
}
