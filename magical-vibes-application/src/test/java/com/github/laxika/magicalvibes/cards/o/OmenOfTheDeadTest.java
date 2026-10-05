package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenOfTheDead.class, NyxbornColossus.class})
class OmenOfTheDeadTest extends BaseCardTest {

    @Test
    void enteringBattlefieldReturnsTargetCreatureFromGraveyardToHand() {
        Card creature = new NyxbornColossus();
        Card nonCreature = new OmenOfTheDead();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.castFromHand(player1, new OmenOfTheDead(), "{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Omen of the Dead");
    }

    @Test
    void sacrificesToScryTwo() {
        Card firstCard = new NyxbornColossus();
        Card secondCard = new NyxbornColossus();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheDead());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(omen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(omen.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
    }

    @Test
    void canBeCastDuringOpponentsEndStepWithoutCreatureTargets() {
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new OmenOfTheDead(), "{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Omen of the Dead");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card opponentCreature = new NyxbornColossus();
        harness.setGraveyard(player1, List.of(new OmenOfTheDead()));
        harness.setGraveyard(player2, List.of(opponentCreature));

        harness.castFromHand(player1, new OmenOfTheDead(), "{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Omen of the Dead");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        harness.assertNotInHand(player1, "Nyxborn Colossus");
    }

    @Test
    void returnTriggerStillResolvesAfterOmenIsSacrificed() {
        Card creature = new NyxbornColossus();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new OmenOfTheDead(), "{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Omen of the Dead");
        harness.assertInGraveyard(player1, "Omen of the Dead");
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Colossus");
        harness.assertNotInGraveyard(player1, "Nyxborn Colossus");
    }

    @Test
    void scryTwoWithOnlyOneCardInLibrary() {
        Card onlyCard = new NyxbornColossus();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addToBattlefield(player1, new OmenOfTheDead());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        harness.assertInGraveyard(player1, "Omen of the Dead");
    }
}
