package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AbzanGuide;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrueIdentity.class, AbzanGuide.class, GrizzlyBears.class})
class TrueIdentityTest extends BaseCardTest {

    @Test
    void turningTrueIdentityFaceUpScriesThenDraws() {
        Card keptOnBottom = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(keptOnBottom, drawn));
        harness.setHand(player1, List.of(new TrueIdentity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent identity = findPermanent(player1, "True Identity");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(identity));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keptOnBottom);
    }

    @Test
    void triggersOnlyOnceEachTurnForControlledPermanents() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new TrueIdentity());
        Permanent firstGuide = addFaceDownGuide();
        Permanent secondGuide = addFaceDownGuide();
        addFaceUpMana(2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstGuide));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(secondGuide));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
    }

    @Test
    void opponentTurningFaceUpDoesNotConsumeControllersTrigger() {
        Card ownDraw = new TrueIdentity();
        Card secondOwnDraw = new TrueIdentity();
        Card opponentDraw = new TrueIdentity();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(ownDraw, secondOwnDraw));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addToBattlefield(player1, new TrueIdentity());
        Permanent own = addFaceDownIdentity(player1);
        Permanent opponent = addFaceDownIdentity(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opponent));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(own));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownDraw, secondOwnDraw);
    }

    @Test
    void separateCopiesEachTriggerBeforeEitherAbilityResolves() {
        Card firstDraw = new TrueIdentity();
        Card secondDraw = new TrueIdentity();
        Card thirdDraw = new TrueIdentity();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.addToBattlefield(player1, new TrueIdentity());
        Permanent first = addFaceDownIdentity(player1);
        Permanent second = addFaceDownIdentity(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(first));
        assertThat(gd.stack).hasSize(2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(second));
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerLimitResetsOnOpponentsTurn() {
        Card firstDraw = new TrueIdentity();
        Card secondDraw = new TrueIdentity();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new TrueIdentity(), new TrueIdentity()));
        harness.addToBattlefield(player1, new TrueIdentity());
        Permanent first = addFaceDownIdentity(player1);
        Permanent second = addFaceDownIdentity(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(first));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);

        Card nextDraw = new TrueIdentity();
        Card lastDraw = new TrueIdentity();
        Card thirdDraw = new TrueIdentity();
        harness.setLibrary(player1, List.of(nextDraw, lastDraw, thirdDraw));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(second));
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, nextDraw, lastDraw, thirdDraw);
    }

    private Permanent addFaceDownIdentity(Player player) {
        Permanent identity = harness.addToBattlefieldAndReturn(player, new TrueIdentity());
        identity.setFaceDownAsDisguised();
        return identity;
    }

    private Permanent addFaceDownGuide() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new AbzanGuide());
        guide.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return guide;
    }

    private void addFaceUpMana(int count) {
        harness.addMana(player1, ManaColor.COLORLESS, 2 * count);
        harness.addMana(player1, ManaColor.WHITE, count);
        harness.addMana(player1, ManaColor.BLACK, count);
        harness.addMana(player1, ManaColor.GREEN, count);
    }
}
