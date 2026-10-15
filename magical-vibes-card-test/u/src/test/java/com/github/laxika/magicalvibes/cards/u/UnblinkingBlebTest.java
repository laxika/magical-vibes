package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnblinkingBleb.class, ZoeticCavern.class})
class UnblinkingBlebTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Unblinking Bleb face up may scry 2")
    void mayScryWhenItTurnsFaceUp() {
        Card first = new ZoeticCavern();
        Card second = new UnblinkingBleb();
        harness.setLibrary(player1, List.of(first, second));
        Permanent bleb = addFaceDownBleb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Declining Unblinking Bleb's scry does nothing")
    void mayScryCanBeDeclined() {
        Card first = new ZoeticCavern();
        Card second = new UnblinkingBleb();
        harness.setLibrary(player1, List.of(first, second));
        Permanent bleb = addFaceDownBleb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Unblinking Bleb triggers when an opponent's noncreature permanent turns face up")
    void triggersForAnOpponentsPermanent() {
        Card first = new ZoeticCavern();
        Card second = new UnblinkingBleb();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new UnblinkingBleb());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new ZoeticCavern());
        opponentPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opponentPermanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new UnblinkingBleb()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bleb = findPermanent(player1, "Unblinking Bleb");
        assertThat(bleb.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bleb.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("A face-down Unblinking Bleb does not trigger for another permanent")
    void faceDownBlebDoesNotTriggerForAnotherPermanent() {
        addFaceDownBleb(player1);
        Permanent cavern = harness.addToBattlefieldAndReturn(player1, new ZoeticCavern());
        cavern.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cavern));

        assertThat(cavern.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 uses the only card in a one-card library")
    void scryWithOneCardInLibrary() {
        Card onlyCard = new ZoeticCavern();
        harness.setLibrary(player1, List.of(onlyCard));
        Permanent bleb = addFaceDownBleb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting scry with an empty library completes without a card choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent bleb = addFaceDownBleb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bleb));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFaceDownBleb(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bleb = harness.addToBattlefieldAndReturn(player, new UnblinkingBleb());
        bleb.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return bleb;
    }
}
