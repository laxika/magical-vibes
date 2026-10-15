package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonMagusSisters.class, BirdsOfParadise.class})
class SummonMagusSistersTest extends BaseCardTest {

    @Test
    void enteringChoosesTheRandomModeAndTargetsBeforeResolution() {
        harness.addToBattlefield(player1, new BirdsOfParadise());
        harness.addToBattlefield(player2, new BirdsOfParadise());

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new SummonMagusSisters());

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).isNotEmpty();
    }

    @Test
    void counterModesRequireATargetAndDefenseAddsAShield() {
        Set<String> modes = new HashSet<>();

        for (int i = 0; i < 120 && modes.size() < 2; i++) {
            Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonMagusSisters());
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
            Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
            int lifeBefore = gd.playerLifeTotals.get(player1.getId());

            advanceToNextChapter();

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            boolean counterMode = choice.validPermanentIds().contains(creature.getId());
            if (counterMode) {
                assertThat(choice.validPlayerIds()).doesNotContain(player1.getId());
            }
            harness.handlePermanentChosen(player1, counterMode ? creature.getId() : opponent.getId());
            harness.passBothPriorities();

            if (creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 3) {
                assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
                assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
                modes.add("combine");
            } else if (counterMode) {
                assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
                assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
                modes.add("defense");
            }

            gd.playerBattlefields.get(player1.getId()).remove(saga);
            gd.playerBattlefields.get(player1.getId()).remove(creature);
            gd.playerBattlefields.get(player2.getId()).remove(opponent);
        }

        assertThat(modes).containsExactlyInAnyOrder("combine", "defense");
    }

    @Test
    void chaptersRandomlyApplyEachModeWithTheCorrectTargets() {
        Set<String> modes = new HashSet<>();

        for (int i = 0; i < 120 && modes.size() < 3; i++) {
            Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonMagusSisters());
            Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
            Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
            int ownCountersBefore = ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            int lifeBefore = gd.playerLifeTotals.get(player1.getId());

            advanceToNextChapter();

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            UUID targetId = choice.validIds().contains(ownCreature.getId())
                    ? ownCreature.getId() : opposingCreature.getId();
            harness.handlePermanentChosen(player1, targetId);
            harness.passBothPriorities();

            if (ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - ownCountersBefore == 3) {
                modes.add("combine");
            }
            if (gd.playerLifeTotals.get(player1.getId()) - lifeBefore == 3) {
                modes.add("defense");
            }
            if (!gd.playerBattlefields.get(player2.getId()).contains(opposingCreature)) {
                modes.add("fight");
            }

            assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

            gd.playerBattlefields.get(player1.getId()).remove(saga);
            gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
            gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        }

        assertThat(modes).containsExactlyInAnyOrder("combine", "defense", "fight");
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    void eachChapterResolvesAndTheThirdSacrificesTheSaga() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonMagusSisters());

        for (int chapter = 1; chapter <= 3; chapter++) {
            advanceToNextChapter();
            assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(chapter);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            if (choice != null) {
                harness.handlePermanentChosen(player1, saga.getId());
            }
            harness.passBothPriorities();

            if (chapter < 3) {
                assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
            }
        }

        harness.assertNotOnBattlefield(player1, "Summon: Magus Sisters");
        harness.assertInGraveyard(player1, "Summon: Magus Sisters");
    }

    @Test
    void fightDoesNoDamageIfTheSagaLeavesBeforeResolution() {
        boolean observedFight = false;

        for (int i = 0; i < 120 && !observedFight; i++) {
            Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonMagusSisters());
            Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());

            advanceToNextChapter();
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            boolean fight = !choice.validPermanentIds().contains(saga.getId());
            harness.handlePermanentChosen(player1, fight ? opponent.getId() : saga.getId());
            if (fight) {
                gd.playerBattlefields.get(player1.getId()).remove(saga);
            }
            harness.passBothPriorities();

            if (fight) {
                assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
                observedFight = true;
            }
            gd.playerBattlefields.get(player1.getId()).remove(saga);
            gd.playerBattlefields.get(player2.getId()).remove(opponent);
        }

        assertThat(observedFight).isTrue();
    }
}
