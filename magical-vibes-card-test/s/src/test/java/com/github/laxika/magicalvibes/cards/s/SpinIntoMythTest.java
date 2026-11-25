package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.n.NewBenalia;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinIntoMyth.class, NessianCourser.class, NewBenalia.class})
class SpinIntoMythTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature on top, then fateseals 2")
    void putsTargetOnTopThenFatesealsTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        NewBenalia firstCard = new NewBenalia();
        NessianCourser secondCard = new NessianCourser();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal).isNotNull();
        assertThat(fateseal.cards()).containsExactly(target.getCard(), firstCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(
                firstCard, secondCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Nessian Courser");
        harness.assertInGraveyard(player1, "Spin into Myth");
    }

    @Test
    @DisplayName("Fateseal 2 looks at only the remaining card when the library has one card")
    void fatesealUsesRemainingLibrarySize() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal).isNotNull();
        assertThat(fateseal.cards()).containsExactly(target.getCard());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        harness.assertNotOnBattlefield(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NewBenalia());
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting your own creature still fateseals the opponent and permits reordering both top cards")
    void ownCreatureDoesNotChangeFatesealedLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        NewBenalia ownCard = new NewBenalia();
        NewBenalia firstCard = new NewBenalia();
        NessianCourser secondCard = new NessianCourser();
        NewBenalia thirdCard = new NewBenalia();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(firstCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal).isNotNull();
        assertThat(fateseal.cards()).containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target.getCard(), ownCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard, firstCard, thirdCard);
        harness.assertNotOnBattlefield(player1, "Nessian Courser");
        harness.assertInGraveyard(player1, "Spin into Myth");
    }

    @Test
    @DisplayName("Both fatesealed cards can be put on the bottom in the chosen order")
    void putsBothCardsOnBottomInChosenOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        NewBenalia firstCard = new NewBenalia();
        NessianCourser secondCard = new NessianCourser();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard, firstCard, target.getCard());
        harness.assertInGraveyard(player1, "Spin into Myth");
    }

    @Test
    @DisplayName("Does not fateseal when its only target leaves before resolution")
    void doesNotFatesealWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        NewBenalia firstCard = new NewBenalia();
        NessianCourser secondCard = new NessianCourser();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(firstCard, secondCard);
        harness.assertInGraveyard(player1, "Spin into Myth");
        harness.assertInGraveyard(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("In a three-player game the controller chooses whose library to fateseal")
    void controllerChoosesTheOpponentToFateseal() {
        Player player3 = addThirdPlayer();
        Permanent target = harness.addToBattlefieldAndReturn(player3, new NessianCourser());
        NewBenalia firstCard = new NewBenalia();
        harness.setLibrary(player2, List.of(new NessianCourser()));
        harness.setLibrary(player3, List.of(firstCard));
        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal.cards()).containsExactly(target.getCard(), firstCard);
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

}
