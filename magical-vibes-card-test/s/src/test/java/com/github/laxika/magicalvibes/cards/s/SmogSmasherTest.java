package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmogSmasher.class, GrizzlyBears.class, ThayanEvokers.class})
class SmogSmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage conjures a nontoken duplicate of the target into exile with it")
    void combatDamageConjuresNontokenDuplicateIntoSourceTrackedExile() {
        Permanent smogSmasher = addCreatureReady(player1, new SmogSmasher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        ExiledCardEntry duplicate = gd.exiledCards.stream()
                .filter(entry -> smogSmasher.getId().equals(entry.sourcePermanentId()))
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.card().getName()).isEqualTo("Grizzly Bears");
        assertThat(duplicate.card().isToken()).isFalse();
    }

    @Test
    @DisplayName("At max speed the duplicates enter with haste and are sacrificed at the next end step")
    void maxSpeedReturnsDuplicatesWithHasteAndSacrificesThemAtNextEndStep() {
        gd.playerSpeeds.put(player1.getId(), 4);
        Permanent smogSmasher = addCreatureReady(player1, new SmogSmasher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent duplicate = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent != bears)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();
        assertThat(gd.getCardsExiledByPermanent(smogSmasher.getId())).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(duplicate);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({SmogSmasher.class})
    @DisplayName("Several damage dealers conjure one duplicate per Smog Smasher")
    void multipleDamageDealersConjureOnlyOneDuplicatePerSmogSmasher() {
        Permanent first = addCreatureReady(player1, new SmogSmasher());
        Permanent second = addCreatureReady(player1, new SmogSmasher());
        first.setAttacking(true);
        second.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(first.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(second.getId())).hasSize(1);
    }

    @Test
    @CardUsed({SmogSmasher.class})
    @DisplayName("An opponent's creature is duplicated and stays where it is")
    void canConjureAnOpponentsCreatureWithoutMovingTheOriginal() {
        Permanent source = addCreatureReady(player1, new SmogSmasher());
        Permanent target = addCreatureReady(player2, new SmogSmasher());
        source.setAttacking(true);

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.exiledCards).singleElement().satisfies(entry ->
                assertThat(entry.ownerId()).isEqualTo(player1.getId()));
    }

    @Test
    @CardUsed({SmogSmasher.class})
    @DisplayName("Below max speed the duplicates stay exiled")
    void duplicatesStayExiledWhenCombatBeginsBelowMaxSpeed() {
        Permanent source = addCreatureReady(player1, new SmogSmasher());
        source.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, source.getId());
        resolveAllTriggers();
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Smog Smasher")).containsExactly(source);
    }

    @Test
    @DisplayName("Conjuring the duplicate triggers conjure abilities")
    void conjuringTheDuplicateIntoExileTriggersConjureAbilities() {
        addCreatureReady(player1, new SmogSmasher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        bears.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("A duplicate of a face-down creature has only its face-down values")
    void duplicateOfAFaceDownCreatureIsNameless() {
        Permanent source = addCreatureReady(player1, new SmogSmasher());
        Permanent faceDown = addCreatureReady(player2, new GrizzlyBears());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        source.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, faceDown.getId());
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).singleElement().satisfies(duplicate -> {
            assertThat(duplicate.getName()).isEmpty();
            assertThat(duplicate.getPower()).isEqualTo(2);
            assertThat(duplicate.getToughness()).isEqualTo(2);
            assertThat(duplicate.isConjured()).isTrue();
        });
        assertThat(gameLogContains("conjures a duplicate of a face-down permanent into exile.")).isTrue();
    }
}
