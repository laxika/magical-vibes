package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.CardColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwayOfIllusion.class, RagingKavu.class, Forest.class})
class SwayOfIllusionTest extends BaseCardTest {

    @Test
    @DisplayName("Makes all targeted creatures the chosen color and draws a card")
    void changesColorOfAllTargetsAndDraws() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.setHand(player1, List.of(new SwayOfIllusion()));
        harness.setLibrary(player1, List.of(new RagingKavu()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, opposingCreature)).containsExactly(CardColor.BLUE);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("A target that leaves before resolution does not stop the remaining target or draw")
    void ignoresTargetThatLeavesBeforeResolution() {
        Permanent leavingCreature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        Permanent remainingCreature = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.setHand(player1, List.of(new SwayOfIllusion()));
        harness.setLibrary(player1, List.of(new RagingKavu()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(leavingCreature.getId(), remainingCreature.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leavingCreature));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectiveColors(gd, remainingCreature)).containsExactly(CardColor.WHITE);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("Draws a card when no creatures are targeted")
    void drawsWithNoTargets() {
        harness.setLibrary(player1, List.of(new RagingKavu()));

        harness.castFromHand(player1, new SwayOfIllusion(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SwayOfIllusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Removes the color change at the end of the turn")
    void colorChangeEndsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        harness.setHand(player1, List.of(new SwayOfIllusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, creature))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.RED);
    }
}
