package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistformMutant;
import com.github.laxika.magicalvibes.cards.z.ZombieBrute;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault12TheNecropolis.class, ZombieBrute.class, MistformMutant.class, GrizzlyBears.class})
class Vault12TheNecropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gives each player three rad counters")
    void chapterIGivesEachPlayerThreeRadCounters() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter II creates Zombie Mutant tokens equal to all players' rad counters")
    void chapterIICreatesTokensEqualToTotalRadCounters() {
        addSagaWithLore(1);
        gd.playerRadCounters.put(player1.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Mutant")).hasSize(3);
        assertThat(findPermanents(player1, "Zombie Mutant"))
                .allSatisfy(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.MUTANT);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Chapter III puts two +1/+1 counters on your Zombies and Mutants")
    void chapterIIIPutsCountersOnOwnZombiesAndMutants() {
        addSagaWithLore(2);
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new ZombieBrute());
        Permanent mutant = harness.addToBattlefieldAndReturn(player1, new MistformMutant());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingZombie = harness.addToBattlefieldAndReturn(player2, new ZombieBrute());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(zombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(mutant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(nonmatching.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingZombie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entering triggers chapter I and adds to existing rad counters")
    void enteringTriggersChapterIAndAddsToExistingRadCounters() {
        gd.playerRadCounters.put(player1.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 4);
        harness.castFromHand(player1, new Vault12TheNecropolis(), "{4}{B}{B}");

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vault 12: The Necropolis")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Chapter II creates no tokens when neither player has rad counters")
    void chapterIICreatesNoTokensWithoutRadCounters() {
        addSagaWithLore(1);
        gd.playerRadCounters.clear();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Mutant")).isEmpty();
        assertThat(findPermanents(player2, "Zombie Mutant")).isEmpty();
    }

    @Test
    @DisplayName("Chapter II counts rad counters at resolution")
    void chapterIICountsRadCountersAtResolution() {
        addSagaWithLore(1);
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 2);

        advanceToNextChapter();
        assertThat(gd.stack).hasSize(1);
        gd.playerRadCounters.put(player1.getId(), 4);
        gd.playerRadCounters.put(player2.getId(), 1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie Mutant")).hasSize(5);
        assertThat(findPermanents(player2, "Zombie Mutant")).isEmpty();
    }

    @Test
    @DisplayName("Chapter III gives a Zombie Mutant only two counters and then sacrifices the Saga")
    void chapterIIICountsBothSubtypesOnlyOnceAndSacrificesSaga() {
        addSagaWithLore(1);
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Zombie Mutant");

        advanceToNextChapter();
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Vault 12: The Necropolis");
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Vault 12: The Necropolis");
        harness.assertInGraveyard(player1, "Vault 12: The Necropolis");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault12TheNecropolis());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
