package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernalRebirth.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class, Forest.class})
class InfernalRebirthTest extends BaseCardTest {

    @Test
    void returnsOneOrTwoTargetCreatureCards() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(second.getId());
    }

    @Test
    void returnsTwoTargetCreatureCards() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new LeoninScimitar()));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void basicLandcyclingSearchesForABasicLand() {
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Infernal Rebirth");
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void cannotCastWithoutACreatureInYourGraveyard() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Infernal Rebirth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseZeroTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsRemainingTargetWhenAnotherLeavesTheGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.setGraveyard(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(second.getId());
        harness.assertInGraveyard(player1, "Infernal Rebirth");
    }

    @Test
    void basicLandcyclingDiscardsAsACostAndMayFailToFind() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.setLibrary(player1, List.of(forest, creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Infernal Rebirth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void basicLandcyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Infernal Rebirth");
        harness.assertNotInGraveyard(player1, "Infernal Rebirth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReturnNewCardsWhenAllTargetsLeaveTheGraveyard() {
        Card target = new GrizzlyBears();
        Card replacement = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(replacement));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Infernal Rebirth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void basicLandcyclingResolvesWithoutABasicLandInTheLibrary() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new InfernalRebirth()));
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Infernal Rebirth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }
}
