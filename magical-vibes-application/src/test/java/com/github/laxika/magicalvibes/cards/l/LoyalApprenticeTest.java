package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.y.YargleGluttonOfUrborg;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalApprentice.class, YargleGluttonOfUrborg.class})
class LoyalApprenticeTest extends BaseCardTest {

    @Test
    void createsHastyThopterWhileControllingCommander() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        advanceToBeginningOfCombat(player1);

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotCreateTokenWithoutControllingCommander() {
        harness.addToBattlefield(player1, new LoyalApprentice());

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    void doesNotCreateTokenOnOpponentsTurn() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        advanceToBeginningOfCombat(player2);

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenCommanderIsAbsentAtBeginningOfCombat() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        beginCombat(player1);

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, commander);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    void doesNotCreateTokenIfCommanderLeavesBeforeResolution() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        beginCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getId().equals(commander.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    void controllingOpponentsCommanderDoesNotSatisfyLieutenant() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        advanceToBeginningOfCombat(player1);

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    void triggerStillCreatesTokenAfterApprenticeLeavesBattlefield() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        beginCombat(player1);
        assertThat(gd.stack).hasSize(1);
        Permanent apprentice = findPermanent(player1, "Loyal Apprentice");
        gd.playerBattlefields.get(player1.getId()).remove(apprentice);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Thopter"), Keyword.HASTE)).isTrue();
    }

    @Test
    void tokenLosesHasteAfterTurnButKeepsFlying() {
        Card commander = new YargleGluttonOfUrborg();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player1, commander);
        harness.addToBattlefield(player1, new LoyalApprentice());

        advanceToBeginningOfCombat(player1);
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(findPermanents(player1, "Thopter")).containsExactly(thopter);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        beginCombat(activePlayer);
        harness.passBothPriorities();
    }
}
