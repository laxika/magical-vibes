package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.c.ChimeOfNight;
import com.github.laxika.magicalvibes.cards.f.FlameJet;
import com.github.laxika.magicalvibes.cards.t.TwistedExperiment;
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

@CardUsed({ScentOfNightshade.class, AncientSilverback.class, BraidwoodCup.class,
        ChimeOfNight.class, FlameJet.class, TwistedExperiment.class})
class ScentOfNightshadeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -X/-X for the number of selected black cards")
    void givesTargetCreatureMinusForSelectedBlackCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        TwistedExperiment blackCard = new TwistedExperiment();
        harness.setHand(player1, List.of(new ScentOfNightshade(), blackCard, new FlameJet()));
        addSpellMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Allows revealing zero black cards")
    void allowsRevealingZeroBlackCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        harness.setHand(player1, List.of(new ScentOfNightshade(), new FlameJet()));
        addSpellMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can choose zero black cards when black cards are available")
    void canChooseZeroBlackCardsWhenBlackCardsAreAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        TwistedExperiment blackCard = new TwistedExperiment();
        harness.setHand(player1, List.of(new ScentOfNightshade(), blackCard, new FlameJet()));
        addSpellMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new ScentOfNightshade()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gives target creature -2/-2 for two selected black cards")
    void givesTargetCreatureMinusTwoForTwoSelectedBlackCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        TwistedExperiment firstBlackCard = new TwistedExperiment();
        ChimeOfNight secondBlackCard = new ChimeOfNight();
        harness.setHand(player1, List.of(
                new ScentOfNightshade(), firstBlackCard, secondBlackCard, new FlameJet()));
        addSpellMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBlackCard.getId(), secondBlackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The creature gets its base stats back at cleanup")
    void minusMinusWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        TwistedExperiment blackCard = new TwistedExperiment();
        harness.setHand(player1, List.of(new ScentOfNightshade(), blackCard));
        addSpellMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
