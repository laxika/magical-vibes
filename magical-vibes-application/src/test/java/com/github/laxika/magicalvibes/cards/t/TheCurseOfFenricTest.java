package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCurseOfFenric.class, GrizzlyBears.class, ClockworkDroid.class, Card.class})
class TheCurseOfFenricTest extends BaseCardTest {

    @Test
    void chapterIDestroysOneCreaturePerControllerAndCreatesMutants() {
        Permanent saga = addSaga(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId(), opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(mutants(player1)).hasSize(1);
        assertThat(mutants(player2)).hasSize(1);
        assertThat(mutants(player1)).allMatch(mutant -> gqs.hasKeyword(gd, mutant, Keyword.DEATHTOUCH));
        assertThat(mutants(player2)).allMatch(mutant -> gqs.hasKeyword(gd, mutant, Keyword.DEATHTOUCH));
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterIITurnsNontokenCreatureIntoFenric() {
        addSaga(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveName(gd, creature)).isEqualTo("Fenric");
        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.HORROR);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasLostAllAbilities(gd, creature)).isTrue();
    }

    @Test
    void chapterIIIMutantFightsFenric() {
        addSaga(2);
        Permanent mutant = harness.addToBattlefieldAndReturn(player1, creature("Mutant", CardSubtype.MUTANT));
        Permanent fenric = harness.addToBattlefieldAndReturn(player2, creature("Fenric"));

        triggerChapter();
        harness.handlePermanentChosen(player1, mutant.getId());
        harness.handlePermanentChosen(player1, fenric.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fenric);
        assertThat(mutant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void chapterIIIRetainsBothTargetsWhenTheyHaveTheSameController() {
        Permanent saga = addSaga(0);
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent futureFenric = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());

        triggerChapter();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        Permanent mutant = mutants(player1).getFirst();

        triggerChapter();
        harness.handlePermanentChosen(player1, futureFenric.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveName(gd, futureFenric)).isEqualTo("Fenric");

        triggerChapter();
        harness.handlePermanentChosen(player1, mutant.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(futureFenric.getId());
        harness.handlePermanentChosen(player1, futureFenric.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(mutant, futureFenric, saga);
    }

    @Test
    void chapterICreatesReplacementMutantForDestroyedToken() {
        Permanent saga = addSaga(0);
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        triggerChapter();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        Permanent originalMutant = mutants(player1).getFirst();

        saga.setCounterCount(CounterType.LORE, 0);
        triggerChapter();
        harness.handlePermanentChosen(player1, originalMutant.getId());
        harness.passBothPriorities();

        assertThat(mutants(player1)).hasSize(1).doesNotContain(originalMutant);
        assertThat(gqs.getEffectivePower(gd, mutants(player1).getFirst())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mutants(player1).getFirst())).isEqualTo(3);
    }

    @Test
    void chapterIIReplacesArtifactTypeAndPersistsAfterSagaLeaves() {
        Permanent saga = addSaga(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        triggerChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, creature)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, creature, CardSupertype.LEGENDARY)).isTrue();
        triggerChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gqs.getEffectiveName(gd, creature)).isEqualTo("Fenric");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasLostAllAbilities(gd, creature)).isTrue();
    }

    @Test
    void chapterIIIDoesNotFightWhenFenricHasAnotherNameAtResolution() {
        addSaga(0);
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent fenric = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        triggerChapter();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        Permanent mutant = mutants(player1).getFirst();

        triggerChapter();
        harness.handlePermanentChosen(player1, fenric.getId());
        harness.passBothPriorities();
        triggerChapter();
        harness.handlePermanentChosen(player1, mutant.getId());
        harness.handlePermanentChosen(player1, fenric.getId());
        fenric.setPersistentName("Clockwork Droid");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mutant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(fenric);
        assertThat(mutant.getMarkedDamage()).isZero();
        assertThat(fenric.getMarkedDamage()).isZero();
    }

    @Test
    void chapterICanChooseNoCreatures() {
        addSaga(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(mutants(player1)).isEmpty();
        assertThat(mutants(player2)).isEmpty();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCurseOfFenric());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private java.util.List<Permanent> mutants(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.MUTANT))
                .toList();
    }

    private Card creature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(java.util.List.of(subtypes));
        return card;
    }
}
