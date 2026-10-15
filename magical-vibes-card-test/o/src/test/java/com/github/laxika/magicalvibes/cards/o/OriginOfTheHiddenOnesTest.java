package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.c.CleopatraExiledPharaoh;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfTheHiddenOnes.class, AssassinInitiate.class, CleopatraExiledPharaoh.class})
class OriginOfTheHiddenOnesTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I deals 4 damage to the chosen target")
    void chapterIDealsDamageToTarget() {
        addSagaWithLore(0);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Chapter II creates two Assassin tokens")
    void chapterIICreatesAssassinTokens() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(assassinTokens()).hasSize(2);
    }

    @Test
    void chapterIICreatesUntappedBlackOneOneAssassinsWithMenace() {
        addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(assassinTokens()).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ASSASSIN);
            assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
        });
    }

    @Test
    @DisplayName("Chapter III creates a tapped attacking token for each attacking Assassin")
    void chapterIIICreatesTokenForEachAttackingAssassin() {
        addSagaWithLore(2);
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        Permanent nonAssassin = addCreatureReady(player1, new CleopatraExiledPharaoh());

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(assassin),
                    gd.playerBattlefields.get(player1.getId()).indexOf(nonAssassin)));
            resolveAllTriggers();
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(assassinTokens()).hasSize(1)
                    .allSatisfy(token -> {
                        assertThat(token.isTapped()).isTrue();
                        assertThat(token.isAttacking()).isTrue();
                        assertThat(token.isAttackedThisTurn()).isFalse();
                        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
                    });
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    void chapterIDestroysCreatureWithFourToughness() {
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CleopatraExiledPharaoh());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Cleopatra, Exiled Pharaoh");
        harness.assertInGraveyard(player2, "Cleopatra, Exiled Pharaoh");
    }

    @Test
    void chapterITriggersWhenSagaEnters() {
        harness.castFromHand(player1, new OriginOfTheHiddenOnes(), "{3}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(findPermanent(player1, "Origin of the Hidden Ones").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void chapterIIICreatesSeparateTokensForTwoAssassinsAfterSagaIsSacrificed() {
        addSagaWithLore(2);
        Permanent first = addCreatureReady(player1, new AssassinInitiate());
        Permanent second = addCreatureReady(player1, new AssassinInitiate());
        advanceToNextChapter();
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Origin of the Hidden Ones");
        harness.assertInGraveyard(player1, "Origin of the Hidden Ones");

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(first),
                    gd.playerBattlefields.get(player1.getId()).indexOf(second)));
            resolveAllTriggers();
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(assassinTokens()).hasSize(2).allSatisfy(token -> {
                assertThat(token.isTapped()).isTrue();
                assertThat(token.isAttacking()).isTrue();
                assertThat(token.isAttackedThisTurn()).isFalse();
            });
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    void chapterIIIDoesNotTriggerForOpponentsAssassin() {
        addSagaWithLore(2);
        Permanent assassin = addCreatureReady(player2, new AssassinInitiate());
        advanceToNextChapter();
        resolveAllTriggers();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(assassin)));
        resolveAllTriggers();

        assertThat(assassinTokens()).isEmpty();
        assertThat(findPermanents(player2, "Assassin")).isEmpty();
    }

    @Test
    void chapterIIIDelayedAbilityExpiresAfterTheTurn() {
        addSagaWithLore(2);
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        advanceToNextChapter();
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(assassin)));
        resolveAllTriggers();

        assertThat(assassinTokens()).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfTheHiddenOnes());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Permanent> assassinTokens() {
        return findPermanents(player1, "Assassin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
