package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheRani.class, GrizzlyBears.class, SwordsToPlowshares.class})
class TheRaniTest extends BaseCardTest {

    @Test
    void entersWithMarkAttachedToAnotherCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRani();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent mark = findPermanent(player1, "Mark of the Rani");
        assertThat(mark.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    void attackingCreatesAnotherMark() {
        Permanent rani = addCreatureReady(player1, new TheRani());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rani)));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mark of the Rani")).hasSize(1);
        assertThat(findPermanent(player1, "Mark of the Rani").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void goadedCreatureCombatDamageInvestigates() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRani();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void markCanEnchantAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castRani();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mark of the Rani").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
        assertThat(findPermanents(player2, "Mark of the Rani")).isEmpty();
    }

    @Test
    void cannotTargetTheRaniItself() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRani();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                findPermanent(player1, "The Rani").getId())).isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Mark of the Rani").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void noMarkIsCreatedWithoutAnotherCreature() {
        castRani();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Rani");
        assertThat(findPermanents(player1, "Mark of the Rani")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void targetLeavingBeforeResolutionPreventsMarkCreation() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRani();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Mark of the Rani")).isEmpty();
    }

    @Test
    void markKeepsBoostingAndGoadingAfterTheRaniLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRani();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        Permanent rani = findPermanent(player1, "The Rani");

        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, rani.getId());

        harness.assertNotOnBattlefield(player1, "The Rani");
        assertThat(findPermanent(player1, "Mark of the Rani").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    void ungoadedCreatureCombatDamageDoesNotInvestigate() {
        addCreatureReady(player1, new TheRani());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void goadedCreatureDamagingRanisControllerDoesNotInvestigate() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castRani();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        findPermanent(player1, "The Rani").tap();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void multipleMarksStackBoostsButInvestigateOnlyOncePerDamageEvent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRani();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        Permanent rani = findPermanent(player1, "The Rani");
        rani.setSummoningSick(false);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rani),
                gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mark of the Rani")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        harness.assertLife(player2, 11);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private void castRani() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TheRani(), "{1}{U}{B}{R}");
    }
}
