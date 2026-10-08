package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EmptyTheWarrens;
import com.github.laxika.magicalvibes.cards.g.GalvanicRelay;
import com.github.laxika.magicalvibes.cards.g.Grapeshot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChargedConjuration.class, EmptyTheWarrens.class, GalvanicRelay.class,
        Grapeshot.class, GrizzlyBears.class, Snapback.class})
class ChargedConjurationTest extends BaseCardTest {

    @Test
    void upkeepMakesInstantAndSorceryInHandCostOneLess() {
        addCreatureReady(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new EmptyTheWarrens(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
    }

    @Test
    void sacrificeOffersSpellbookCardAndConjuresSelectedCard() {
        addCreatureReady(player1, new ChargedConjuration());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        UUID selectedId = choice.validCardIds().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selectedId));

        assertThat(countPermanents(player1, "Charged Conjuration")).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Empty the Warrens");
    }

    @Test
    void repeatedUpkeepsStackReductionsAndSacrificingSourceDoesNotRemoveThem() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new EmptyTheWarrens()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Charged Conjuration");
        harness.assertInGraveyard(player1, "Charged Conjuration");
    }

    @Test
    void upkeepReducesInstantCostAsWell() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Snapback()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Snapback");
    }

    @Test
    void opponentUpkeepDoesNotReduceYourSpells() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new EmptyTheWarrens()));
        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Empty the Warrens");
    }

    @Test
    void creatureCardsDoNotReceiveDiscount() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void discountAppliesToCardsInHandWhenTriggerResolves() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.setHand(player1, List.of());
        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new EmptyTheWarrens()));
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
    }

    @Test
    void discountCannotPayColoredManaEvenAfterRepeatedUpkeeps() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new Grapeshot()));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 19);
    }

    @Test
    void sacrificeCannotBeActivatedWithSpellOnStack() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.setHand(player1, List.of(new EmptyTheWarrens()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Charged Conjuration");
        resolveAllTriggers();
    }

    @Test
    void sacrificeCannotBeActivatedDuringUpkeep() {
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Charged Conjuration");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Empty the Warrens", "Galvanic Relay", "Grapeshot"})
    void everySpellbookCardCanBeChosen(String cardName) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new ChargedConjuration());
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Charged Conjuration");
        resolveAllTriggers();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice.cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Empty the Warrens", "Galvanic Relay", "Grapeshot");
        UUID selectedId = choice.cards().stream()
                .filter(card -> card.getName().equals(cardName))
                .findFirst().orElseThrow().getId();
        harness.handleMultipleCardsChosen(player1, List.of(selectedId));

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo(cardName);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
