package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmogSmasher.class, GrizzlyBears.class})
class SmogSmasherTest extends BaseCardTest {

    @Test
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
}
