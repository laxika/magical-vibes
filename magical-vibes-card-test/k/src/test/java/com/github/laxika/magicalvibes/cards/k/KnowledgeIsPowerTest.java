package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnowledgeIsPower.class, GrizzlyBears.class, Opalescence.class})
class KnowledgeIsPowerTest extends BaseCardTest {

    @Test
    void boostsYourCreaturesByCardsDrawnThisTurn() {
        harness.addToBattlefield(player1, new KnowledgeIsPower());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);

        draw(player1);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void countsDrawsBeforeEnchantmentEntersAndBoostsLaterCreatures() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        draw(player1);

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        harness.addToBattlefield(player1, new KnowledgeIsPower());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(4);
    }

    @Test
    void multipleCopiesEachApplyTheCurrentDrawCount() {
        harness.addToBattlefield(player1, new KnowledgeIsPower());
        harness.addToBattlefield(player1, new KnowledgeIsPower());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void bonusResetsOnNextTurnAndCountsDrawsDuringOpponentsTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new KnowledgeIsPower());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void boostsItselfWhenItBecomesACreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new KnowledgeIsPower());
        harness.addToBattlefield(player1, new Opalescence());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.isCreature(gd, enchantment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(5);
        draw(player1);
        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(6);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
