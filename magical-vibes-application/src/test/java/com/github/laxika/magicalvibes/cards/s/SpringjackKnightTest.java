package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringjackKnight.class, KithkinGreatheart.class, Forest.class})
class SpringjackKnightTest extends BaseCardTest {

    // ===== Attack trigger: target selection =====

    @Test
    @DisplayName("Attacking queues attack trigger for target selection")
    void attackTriggersTargetSelection() {
        addCreatureReady(player1, new SpringjackKnight());
        addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Attack trigger can target any creature, but not a land")
    void attackTriggerTargetsAnyCreature() {
        addCreatureReady(player1, new SpringjackKnight());
        Permanent ownCreature = addCreatureReady(player1, new KithkinGreatheart());
        Permanent opposingCreature = addCreatureReady(player2, new KithkinGreatheart());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId(), opposingCreature.getId())
                .doesNotContain(opposingLand.getId());
    }

    @Test
    @DisplayName("Choosing target puts the clash trigger on the stack")
    void choosingTargetPutsTriggerOnStack() {
        Permanent knight = addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Springjack Knight")
                        && se.getTargetId().equals(ally.getId())
                        && se.getSourcePermanentId().equals(knight.getId()));
    }

    // ===== Won clash — target gains double strike =====

    @Test
    @DisplayName("Winning the clash grants double strike to the target creature")
    void wonClashGrantsDoubleStrike() {
        // Higher mana value on top for player1 (Kithkin Greatheart MV 2 > Forest MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        keepBothRevealedCardsOnTop();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    // ===== Lost clash — no grant =====

    @Test
    @DisplayName("Losing the clash grants nothing")
    void lostClashGrantsNothing() {
        // Lower mana value on top for player1 (Forest MV 0 < Kithkin Greatheart MV 2) → player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        keepBothRevealedCardsOnTop();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Tie — a clash is only won on a strictly greater mana value =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so nothing is granted")
    void tiedClashGrantsNothing() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        keepBothRevealedCardsOnTop();

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        addCreatureReady(player1, new SpringjackKnight());
        Permanent ally = addCreatureReady(player1, new KithkinGreatheart());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        keepBothRevealedCardsOnTop();
        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(ally.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }


    private void keepBothRevealedCardsOnTop() {
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("The attacking Knight can grant itself double strike")
    void canTargetItself() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent knight = addCreatureReady(player1, new SpringjackKnight());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, knight.getId());
        harness.passBothPriorities();
        keepBothRevealedCardsOnTop();
        assertThat(knight.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A won clash can grant double strike to an opposing creature")
    void canGrantDoubleStrikeToOpponent() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));
        addCreatureReady(player1, new SpringjackKnight());
        Permanent target = addCreatureReady(player2, new KithkinGreatheart());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        keepBothRevealedCardsOnTop();
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An illegal target prevents the entire ability from resolving, including the clash")
    void missingTargetPreventsClash() {
        KithkinGreatheart ownTop = new KithkinGreatheart();
        Forest opposingTop = new Forest();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingTop));
        addCreatureReady(player1, new SpringjackKnight());
        Permanent target = addCreatureReady(player1, new KithkinGreatheart());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        assertThat(gd.lastClashWonByController).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Revealed cards move only after both players decide their placement")
    void clashCardsMoveSimultaneously() {
        KithkinGreatheart winningCard = new KithkinGreatheart();
        Forest ownNext = new Forest();
        Forest losingCard = new Forest();
        KithkinGreatheart opposingNext = new KithkinGreatheart();
        harness.setLibrary(player1, List.of(winningCard, ownNext));
        harness.setLibrary(player2, List.of(losingCard, opposingNext));
        addCreatureReady(player1, new SpringjackKnight());
        Permanent target = addCreatureReady(player1, new KithkinGreatheart());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(winningCard, ownNext);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownNext, winningCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingNext, losingCard);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

}
