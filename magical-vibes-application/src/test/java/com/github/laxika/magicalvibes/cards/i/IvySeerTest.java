package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.y.YavimayaElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IvySeer.class, PlatedSpider.class, YavimayaElder.class, BraidwoodCup.class})
class IvySeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature +X/+X for the number of selected green cards")
    void givesTargetCreaturePlusForSelectedGreenCards() {
        addReadySeer();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        YavimayaElder firstGreenCard = new YavimayaElder();
        YavimayaElder secondGreenCard = new YavimayaElder();
        BraidwoodCup nonGreenCard = new BraidwoodCup();
        harness.setHand(player1, List.of(firstGreenCard, secondGreenCard, nonGreenCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(firstGreenCard.getId(), secondGreenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId(), secondGreenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstGreenCard, secondGreenCard, nonGreenCard);
    }

    @Test
    @DisplayName("Allows revealing zero green cards")
    void allowsRevealingZeroGreenCards() {
        addReadySeer();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        harness.setHand(player1, List.of(new BraidwoodCup()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Allows choosing zero green cards when green cards are available")
    void allowsChoosingZeroGreenCardsWhenAvailable() {
        Permanent seer = addReadySeer();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        YavimayaElder greenCard = new YavimayaElder();
        harness.setHand(player1, List.of(greenCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(greenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Uses only the selected green cards when several are available")
    void usesOnlySelectedGreenCardsWhenSeveralAreAvailable() {
        addReadySeer();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        YavimayaElder firstGreenCard = new YavimayaElder();
        YavimayaElder secondGreenCard = new YavimayaElder();
        harness.setHand(player1, List.of(firstGreenCard, secondGreenCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(firstGreenCard.getId(), secondGreenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadySeer();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        YavimayaElder greenCard = new YavimayaElder();
        harness.setHand(player1, List.of(greenCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadySeer();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new YavimayaElder()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySeer() {
        return addCreatureReady(player1, new IvySeer());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
