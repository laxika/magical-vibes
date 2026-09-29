package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRani.class, GrizzlyBears.class})
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

    private void castRani() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TheRani()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
