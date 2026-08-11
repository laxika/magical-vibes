package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LavakinBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the Elemental-count trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new LavakinBrawler());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Gets +1/+0 for each Elemental controlled")
    void boostScalesWithControlledElementals() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());
        addCreatureReady(player1, new FireElemental());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new FireElemental());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(brawler.getPowerModifier()).isEqualTo(2);
        assertThat(brawler.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent brawler = addCreatureReady(player1, new LavakinBrawler());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(brawler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brawler.getPowerModifier()).isEqualTo(0);
    }
}
