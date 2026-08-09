package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScentOfNightshadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -X/-X for the number of selected black cards")
    void givesTargetCreatureMinusForSelectedBlackCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(new ScentOfNightshade(), blackCard, new Shock()));
        addSpellMana();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Allows revealing zero black cards")
    void allowsRevealingZeroBlackCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScentOfNightshade(), new Shock()));
        addSpellMana();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ScentOfNightshade(), new DarkRitual()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature gets its base stats back at cleanup")
    void minusMinusWearsOffAtCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(new ScentOfNightshade(), blackCard));
        addSpellMana();

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
