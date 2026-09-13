package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.f.FlameJet;
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

@CardUsed({ScentOfIvy.class, AncientSilverback.class, FlameJet.class, BraidwoodCup.class})
class ScentOfIvyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +X/+X for the number of selected green cards")
    void givesTargetCreaturePlusForSelectedGreenCards() {
        Permanent silverback = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        AncientSilverback greenCard = new AncientSilverback();
        harness.setHand(player1, List.of(new ScentOfIvy(), greenCard, new FlameJet()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, silverback.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(greenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(6);
        harness.assertInHand(player1, "Ancient Silverback");
        harness.assertInHand(player1, "Flame Jet");
    }

    @Test
    @DisplayName("Allows revealing zero green cards")
    void allowsRevealingZeroGreenCards() {
        Permanent silverback = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        harness.setHand(player1, List.of(new ScentOfIvy(), new FlameJet()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, silverback.getId());

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can choose zero green cards when green cards are available")
    void canChooseZeroGreenCardsWhenGreenCardsAreAvailable() {
        Permanent silverback = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        AncientSilverback greenCard = new AncientSilverback();
        harness.setHand(player1, List.of(new ScentOfIvy(), greenCard, new FlameJet()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, silverback.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(greenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gives target creature +X/+X for multiple selected green cards")
    void givesTargetCreaturePlusForMultipleSelectedGreenCards() {
        Permanent silverback = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        AncientSilverback firstGreenCard = new AncientSilverback();
        AncientSilverback secondGreenCard = new AncientSilverback();
        harness.setHand(player1,
                List.of(new ScentOfIvy(), firstGreenCard, secondGreenCard, new FlameJet()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, silverback.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstGreenCard.getId(), secondGreenCard.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(firstGreenCard.getId(), secondGreenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(7);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent silverback = harness.addToBattlefieldAndReturn(player2, new AncientSilverback());
        AncientSilverback greenCard = new AncientSilverback();
        harness.setHand(player1, List.of(new ScentOfIvy(), greenCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, silverback.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(7);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new ScentOfIvy(), new FlameJet()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
